package com.onestop.catalog.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable pagination envelope. We map Spring Data's {@link Page} into this rather
 * than serializing PageImpl directly (whose JSON shape is unstable across versions).
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PagedResponse<T> from(Page<T> p) {
        return new PagedResponse<>(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.isLast()
        );
    }
}
