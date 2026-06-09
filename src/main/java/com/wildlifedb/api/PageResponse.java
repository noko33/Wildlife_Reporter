package com.wildlifedb.api;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public class PageResponse<T> {

    private final long totalElements;
    private final int totalPages;
    private final int page;
    private final int size;
    private final List<T> items;

    public PageResponse(long totalElements, int totalPages, int page, int size, List<T> items) {
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
        this.items = items;
    }

    public static <S, T> PageResponse<T> from(Page<S> source, Function<S, T> mapper) {
        return new PageResponse<>(
                source.getTotalElements(),
                source.getTotalPages(),
                source.getNumber(),
                source.getSize(),
                source.getContent().stream().map(mapper).toList());
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public List<T> getItems() {
        return items;
    }
}
