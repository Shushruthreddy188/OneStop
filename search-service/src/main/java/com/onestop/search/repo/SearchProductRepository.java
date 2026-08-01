package com.onestop.search.repo;

import com.onestop.search.domain.SearchProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SearchProductRepository extends JpaRepository<SearchProduct, Long> {

    /** Interface projection for autocomplete suggestions. */
    interface SuggestionView {
        Long getProductId();
        String getName();
    }

    /** Interface projection for search results. */
    interface ResultView {
        Long getProductId();
        String getName();
        String getBrandName();
        String getCategoryName();
        String getPackageSize();
        BigDecimal getSellingPrice();
        BigDecimal getMrp();
    }

    /**
     * Typo-tolerant autocomplete. Matches either a literal substring OR a trigram
     * similarity above the threshold (the {@code %} operator), so "basmti" still
     * finds "basmati". Ranked by similarity, then shorter names first.
     */
    @Query(value = """
            SELECT product_id AS "productId", name AS "name"
            FROM search_product
            WHERE name ILIKE '%' || :q || '%' OR :q % name
            ORDER BY similarity(name, :q) DESC, length(name), name
            LIMIT :limit
            """, nativeQuery = true)
    List<SuggestionView> suggest(@Param("q") String q, @Param("limit") int limit);

    /** Paginated search with optional (empty-string = skip) query/category/brand filters. */
    @Query(value = """
            SELECT product_id AS "productId", name AS "name", brand_name AS "brandName",
                   category_name AS "categoryName", package_size AS "packageSize",
                   selling_price AS "sellingPrice", mrp AS "mrp"
            FROM search_product
            WHERE (:q = '' OR name ILIKE '%' || :q || '%')
              AND (:category = '' OR category_name = :category)
              AND (:brand = '' OR brand_name = :brand)
            ORDER BY CASE WHEN :q = '' THEN 0 ELSE similarity(name, :q) END DESC, name
            """,
            countQuery = """
            SELECT count(*) FROM search_product
            WHERE (:q = '' OR name ILIKE '%' || :q || '%')
              AND (:category = '' OR category_name = :category)
              AND (:brand = '' OR brand_name = :brand)
            """,
            nativeQuery = true)
    Page<ResultView> search(@Param("q") String q,
                            @Param("category") String category,
                            @Param("brand") String brand,
                            Pageable pageable);
}
