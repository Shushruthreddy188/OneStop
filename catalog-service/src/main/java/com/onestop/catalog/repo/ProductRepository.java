package com.onestop.catalog.repo;

import com.onestop.catalog.domain.Product;
import com.onestop.catalog.web.dto.ProductDetailDto;
import com.onestop.catalog.web.dto.ProductSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Paginated product search with optional category, brand, and name filters.
     * Each filter is skipped when its parameter is null, so all combinations work
     * through one query. Projects straight into a DTO to avoid lazy loading.
     */
    @Query(value = """
            SELECT new com.onestop.catalog.web.dto.ProductSummaryDto(
                p.id, p.sku, p.name, b.name, c.name, p.packageSize,
                p.imageUrl, p.mrp, p.sellingPrice, p.status)
            FROM Product p
            LEFT JOIN p.brand b
            LEFT JOIN p.category c
            WHERE (:categoryId IS NULL OR c.id = :categoryId OR c.parent.id = :categoryId)
              AND (:brandId IS NULL OR b.id = :brandId)
              AND LOWER(p.name) LIKE LOWER(:namePattern)
            ORDER BY p.name, p.id
            """,
            countQuery = """
            SELECT count(p)
            FROM Product p
            LEFT JOIN p.brand b
            LEFT JOIN p.category c
            WHERE (:categoryId IS NULL OR c.id = :categoryId OR c.parent.id = :categoryId)
              AND (:brandId IS NULL OR b.id = :brandId)
              AND LOWER(p.name) LIKE LOWER(:namePattern)
            """)
    Page<ProductSummaryDto> search(@Param("categoryId") Long categoryId,
                                   @Param("brandId") Long brandId,
                                   @Param("namePattern") String namePattern,
                                   Pageable pageable);

    @Query("""
            SELECT new com.onestop.catalog.web.dto.ProductDetailDto(
                p.id, p.sku, p.name, p.description, b.id, b.name, c.id, c.name,
                p.packageSize, p.imageUrl, p.mrp, p.sellingPrice, p.status)
            FROM Product p
            LEFT JOIN p.brand b
            LEFT JOIN p.category c
            WHERE p.id = :id
            """)
    Optional<ProductDetailDto> findDetailById(@Param("id") Long id);
}
