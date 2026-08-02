package com.onestop.search.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.indices.update_aliases.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Owns the Elasticsearch product index lifecycle.
 *
 * <p>Reindex is zero-downtime: each rebuild writes to a fresh versioned index
 * ({@code products-v<epochMillis>}) and only when it is fully populated is the
 * read alias ({@code products}) atomically flipped to it; the previous index is
 * then dropped. Searches always hit the alias, so a rebuild — or a rollback by
 * re-pointing the alias — is invisible to callers.
 */
@Component
public class ProductIndexService {

    private static final Logger log = LoggerFactory.getLogger(ProductIndexService.class);

    /** Read/write alias every query targets. */
    public static final String ALIAS = "products";
    private static final String MAPPING_RESOURCE = "es/product-index.json";

    private final ElasticsearchClient es;

    public ProductIndexService(ElasticsearchClient es) {
        this.es = es;
    }

    /** Cheap liveness probe so callers can fall back to Postgres when ES is down. */
    public boolean isAvailable() {
        try {
            return es.ping().value();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Build a brand-new index from the given documents and swap the alias onto
     * it. Returns the number of documents indexed.
     */
    public long reindex(List<ProductDoc> docs) throws IOException {
        String newIndex = ALIAS + "-v" + System.currentTimeMillis();
        createIndex(newIndex);

        long indexed = bulkIndex(newIndex, docs);

        Set<String> previous = indicesForAlias();
        swapAlias(newIndex, previous);
        deleteIndices(previous);

        log.info("Reindex complete: {} docs into {} (alias '{}' swapped, dropped {})",
                indexed, newIndex, ALIAS, previous);
        return indexed;
    }

    private void createIndex(String index) throws IOException {
        try (InputStream mapping = new ClassPathResource(MAPPING_RESOURCE).getInputStream()) {
            es.indices().create(c -> c.index(index).withJson(mapping));
        }
    }

    private long bulkIndex(String index, List<ProductDoc> docs) throws IOException {
        if (docs.isEmpty()) {
            return 0;
        }
        BulkRequest.Builder br = new BulkRequest.Builder().refresh(co.elastic.clients.elasticsearch._types.Refresh.True);
        for (ProductDoc d : docs) {
            br.operations(op -> op.index(idx -> idx
                    .index(index)
                    .id(String.valueOf(d.productId()))
                    .document(d)));
        }
        BulkResponse resp = es.bulk(br.build());
        if (resp.errors()) {
            resp.items().stream()
                    .filter(i -> i.error() != null)
                    .findFirst()
                    .ifPresent(i -> log.error("Bulk index error on id {}: {}", i.id(),
                            i.error() != null ? i.error().reason() : "?"));
        }
        return docs.size();
    }

    /** Indices currently behind the alias (empty on the very first reindex). */
    private Set<String> indicesForAlias() throws IOException {
        try {
            return es.indices().getAlias(a -> a.name(ALIAS)).result().keySet();
        } catch (ElasticsearchException e) {
            if (e.status() == 404) {
                return Set.of();
            }
            throw e;
        }
    }

    private void swapAlias(String newIndex, Set<String> previous) throws IOException {
        List<Action> actions = new ArrayList<>();
        actions.add(Action.of(a -> a.add(ad -> ad.index(newIndex).alias(ALIAS))));
        for (String old : previous) {
            actions.add(Action.of(a -> a.remove(r -> r.index(old).alias(ALIAS))));
        }
        es.indices().updateAliases(u -> u.actions(actions));
    }

    private void deleteIndices(Set<String> indices) throws IOException {
        for (String idx : indices) {
            es.indices().delete(d -> d.index(idx));
        }
    }
}
