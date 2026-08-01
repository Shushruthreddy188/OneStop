package com.onestop.catalog.web;

import com.onestop.catalog.repo.CategoryRepository;
import com.onestop.catalog.web.dto.CategoryDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categories;

    public CategoryController(CategoryRepository categories) {
        this.categories = categories;
    }

    /** GET /api/categories - flat list; parentId is null for top-level categories. */
    @GetMapping
    public List<CategoryDto> list() {
        return categories.findAllDto();
    }
}
