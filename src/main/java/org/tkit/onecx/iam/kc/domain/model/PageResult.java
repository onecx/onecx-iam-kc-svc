package org.tkit.onecx.iam.kc.domain.model;

import java.util.List;

import lombok.Getter;

@Getter
public class PageResult<T> {

    public static PageResult<?> empty() {
        return new PageResult<>(0, List.of(), Page.of(0, 1));
    }

    private final long totalElements;

    private final long number;

    private final long size;

    private final long totalPages;

    private final List<T> stream;

    public PageResult(long totalElements, List<T> stream, Page page) {
        this.totalElements = totalElements;
        this.stream = stream;
        this.number = page.number();
        this.size = page.size();
        this.totalPages = (totalElements + size - 1) / size;
    }

    public boolean isEmpty() {
        return totalElements <= 0;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "c=" + totalElements +
                ",n=" + number +
                ",s=" + size +
                '}';
    }
}
