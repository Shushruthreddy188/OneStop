package com.onestop.catalog.service;

import com.onestop.catalog.repo.CategoryRepository;
import com.onestop.catalog.repo.ProductRepository;
import com.onestop.catalog.web.dto.CategoryDto;
import com.onestop.catalog.web.dto.ProductDetailDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Cached read paths for the catalog. Product detail and the category list change
 * rarely and are read often, so they are good Redis cache candidates. Paginated
 * product search is intentionally not cached (high key cardinality).
 */
@Service
public class CatalogQueryService {

    private final ProductRepository products;
    private final CategoryRepository categories;

    public CatalogQueryService(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    @Cacheable(value = "productDetail", key = "#id", unless = "#result == null")
    public ProductDetailDto getProductOrNull(Long id) {
        return products.findDetailById(id).orElse(null);
    }

    @Cacheable(value = "categories")
    public List<CategoryDto> getCategories() {
        return categories.findAllDto();
    }
}
