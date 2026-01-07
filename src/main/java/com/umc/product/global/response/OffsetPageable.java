package com.umc.product.global.response;

import java.util.List;

public interface OffsetPageable<T> {
    List<T> content();
    int page();
    int size();
    long totalElements();
    int totalPages();
    boolean hasNext();
    boolean hasPrevious();
}
