package io.github.sibu912.snmbank.loans.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A page of results. Used instead of returning Spring's Page object directly,
 * so the JSON shape stays the same whatever Spring version is used.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
