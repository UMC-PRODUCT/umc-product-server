package com.umc.product.global.response;

import java.util.List;

public interface CursorPageable<T> {
    List<T> content();
    Long nextCursor();
    boolean hasNext();
    int size();
}
