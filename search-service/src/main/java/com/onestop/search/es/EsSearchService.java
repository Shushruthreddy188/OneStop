package com.onestop.search.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.onestop.search.web.dto.SearchDtos.Facet;
import com.onestop.search.web.dto.SearchDtos.FacetedSearchResponse;
import com.onestop.search.web.dto.SearchDtos.SearchResultDto;
import com.onestop.search.web.dto.SearchDtos.SuggestionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Executes product search and autocomplete against the Elasticsearch alias. */
@Component
public class EsSearchService {

    private static final Logger log = LoggerFactory.getLogger(EsSearchService.class);

    private final ElasticsearchClient es;
    /** Feature flag: blend behavioural popularity into relevance ranking. */
    private final boolean behavioralRanking;

    public EsSearchService(ElasticsearchClient es,
                           @Value("${onestop.search.ranking.behavioral:false}") boolean behavioralRanking) {
        this.es = es;
        this.behavioralRanking = behavioralRanking;
        log.info("Behavioural ranking (popularity rank_feature) is {}",
                behavioralRanking ? "ON" : "OFF");
    }

    /** Autocomplete via the search_as_you_type sub-field (prefix on the last term). */
    public List<SuggestionDto> suggest(String q, int limit) throws IOException {
        SearchResponse<ProductDoc> resp = es.search(s -> s
                .index(ProductIndexService.ALIAS)
                .size(limit)
                .source(src -> src.filter(f -> f.includes("productId", "name")))
                .query(query -> query.multiMatch(mm -> mm
                        .query(q)
                        .type(TextQueryType.BoolPrefix)
                        .fields("name.autocomplete", "name.autocomplete._2gram",
                                "name.autocomplete._3gram"))), ProductDoc.class);

        List<SuggestionDto> out = new ArrayList<>();
        for (Hit<ProductDoc> h : resp.hits().hits()) {
            ProductDoc d = h.source();
            if (d != null) {
                out.add(new SuggestionDto(d.productId(), d.name()));
            }
        }
        return out;
    }

    /**
     * Full search with typo tolerance (fuzziness AUTO), optional brand/category
     * filters, and brand/category facet counts computed over the query.
     */
    public FacetedSearchResponse search(String q, String category, String brand, int page, int size)
            throws IOException {
        boolean hasQ = q != null && !q.isBlank();
        boolean hasBrand = brand != null && !brand.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        SearchResponse<ProductDoc> resp = es.search(s -> s
                .index(ProductIndexService.ALIAS)
                .from(page * size)
                .size(size)
                .trackTotalHits(t -> t.enabled(true))
                .query(query -> query.bool(b -> {
                    if (hasQ) {
                        b.must(m -> m.multiMatch(mm -> mm
                                .query(q)
                                .fields("name^2", "name.autocomplete")
                                .fuzziness("AUTO")
                                .operator(Operator.And)));
                    } else {
                        b.must(m -> m.matchAll(ma -> ma));
                    }
                    if (hasBrand) {
                        b.filter(f -> f.term(t -> t.field("brandName").value(brand)));
                    }
                    if (hasCategory) {
                        b.filter(f -> f.term(t -> t.field("categoryName").value(category)));
                    }
                    // Behavioural boost: a non-scoring 'should' rank_feature so
                    // popular products float up among equally-relevant matches,
                    // without ever excluding a result. Flag-gated.
                    if (behavioralRanking) {
                        b.should(sh -> sh.rankFeature(rf -> rf
                                .field("popularity")
                                .saturation(sat -> sat)));
                    }
                    return b;
                }))
                .aggregations("brands", a -> a.terms(t -> t.field("brandName").size(20)))
                .aggregations("categories", a -> a.terms(t -> t.field("categoryName").size(20))),
                ProductDoc.class);

        List<SearchResultDto> content = new ArrayList<>();
        for (Hit<ProductDoc> h : resp.hits().hits()) {
            ProductDoc d = h.source();
            if (d != null) {
                content.add(new SearchResultDto(d.productId(), d.name(), d.brandName(),
                        d.categoryName(), d.packageSize(), d.sellingPrice(), d.mrp()));
            }
        }

        long total = resp.hits().total() != null ? resp.hits().total().value() : content.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        boolean last = (long) (page + 1) * size >= total;

        return new FacetedSearchResponse(content, page, size, total, totalPages, last,
                buckets(resp, "brands"), buckets(resp, "categories"));
    }

    private static List<Facet> buckets(SearchResponse<ProductDoc> resp, String aggName) {
        var agg = resp.aggregations().get(aggName);
        if (agg == null || !agg.isSterms()) {
            return List.of();
        }
        List<Facet> facets = new ArrayList<>();
        for (var bucket : agg.sterms().buckets().array()) {
            facets.add(new Facet(bucket.key().stringValue(), bucket.docCount()));
        }
        return facets;
    }
}
