package com.onestop.catalog.web;

import com.onestop.catalog.repo.ProductRepository;
import com.onestop.catalog.web.dto.PagedResponse;
import com.onestop.catalog.web.dto.ProductDetailDto;
import com.onestop.catalog.web.dto.ProductSummaryDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    /** GET /api/products?page=0&size=20&category={id}&brand={id}&q={text} */
    @GetMapping
    public PagedResponse<ProductSummaryDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long brand,
            @RequestParam(required = false) String q) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var result = products.search(category, brand, namePattern(q), PageRequest.of(safePage, safeSize));
        return PagedResponse.from(result);
    }

    /** Build a SQL LIKE pattern; blank/absent search matches everything ("%"). */
    private static String namePattern(String q) {
        return (q == null || q.isBlank()) ? "%" : "%" + q.trim() + "%";
    }

    /** GET /api/products/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDto> get(@PathVariable Long id) {
        return products.findDetailById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
