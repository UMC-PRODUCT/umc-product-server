package com.umc.product.curriculum.application.port.in.query.dto;

import java.util.List;

public record WeeklyBestWorkbookPageInfo(
    List<WeeklyBestWorkbookInfo> content,
    Long nextCursor,
    boolean hasNext
) {

    public static WeeklyBestWorkbookPageInfo of(List<WeeklyBestWorkbookInfo> fetchedContent, int requestedSize) {
        boolean hasNext = fetchedContent.size() > requestedSize;
        List<WeeklyBestWorkbookInfo> content = hasNext
            ? fetchedContent.subList(0, requestedSize)
            : fetchedContent;
        Long nextCursor = hasNext && !content.isEmpty()
            ? content.get(content.size() - 1).weeklyBestWorkbookEntityId()
            : null;
        return new WeeklyBestWorkbookPageInfo(content, nextCursor, hasNext);
    }

    public static WeeklyBestWorkbookPageInfo empty() {
        return new WeeklyBestWorkbookPageInfo(List.of(), null, false);
    }
}
