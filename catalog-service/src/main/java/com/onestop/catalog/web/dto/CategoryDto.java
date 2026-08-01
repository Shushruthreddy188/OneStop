package com.onestop.catalog.web.dto;

/** A category with its parent (null parent = top-level). */
public record CategoryDto(
        Long id,
        String name,
        Long parentId,
        String parentName
) {
}
