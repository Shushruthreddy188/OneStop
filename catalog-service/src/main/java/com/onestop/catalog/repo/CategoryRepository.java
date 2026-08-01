package com.onestop.catalog.repo;

import com.onestop.catalog.domain.Category;
import com.onestop.catalog.web.dto.CategoryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
            SELECT new com.onestop.catalog.web.dto.CategoryDto(
                c.id, c.name, p.id, p.name)
            FROM Category c
            LEFT JOIN c.parent p
            ORDER BY COALESCE(p.name, c.name), c.name
            """)
    List<CategoryDto> findAllDto();
}
