package com.umc.product.blog.adapter.in.web.dto.request;

import java.util.List;

import com.umc.product.blog.application.port.in.command.dto.ReplaceBlogSeriesContentsCommand;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ReplaceBlogSeriesContentsRequest(
    @NotEmpty(message = "contentIds는 비어 있을 수 없습니다.") List<@NotNull(message = "contentId는 null일 수 없습니다.") Long> contentIds
) {
    public ReplaceBlogSeriesContentsCommand toCommand(Long seriesId) {
        return ReplaceBlogSeriesContentsCommand.of(seriesId, contentIds);
    }
}
