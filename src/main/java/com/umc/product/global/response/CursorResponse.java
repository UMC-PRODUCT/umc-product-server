package com.umc.product.global.response;

import java.util.List;
import java.util.function.Function;

public record CursorResponse<T>(List<T> content, Long nextCursor, boolean hasNext) {
    public static <T> CursorResponse<T> of(List<T> content, Long nextCursor, boolean hasNext) {
        return new CursorResponse<>(content, nextCursor, hasNext);
    }

    public static <T, R> CursorResponse<R> of(List<T> content, int requestedSize, Function<T, Long> cursorExtractor,
                                              Function<T, R> mapper) {
        boolean hasNext = content.size() > requestedSize;

        List<T> result = hasNext ? content.subList(0, requestedSize) : content;

        List<R> mappedContent = result.stream().map(mapper).toList();

        Long nextCursor = hasNext && !result.isEmpty() ? cursorExtractor.apply(result.get(result.size() - 1)) : null;

        return new CursorResponse<>(mappedContent, nextCursor, hasNext);
    }

    public static <T> CursorResponse<T> empty() {
        return new CursorResponse<>(List.of(), null, false);
    }
}

