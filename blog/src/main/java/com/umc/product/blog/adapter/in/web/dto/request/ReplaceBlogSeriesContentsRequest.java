package com.umc.product.blog.adapter.in.web.dto.request;

import java.util.List;

import com.umc.product.blog.application.port.in.command.dto.ReplaceBlogSeriesContentsCommand;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ReplaceBlogSeriesContentsRequest(
    @NotEmpty(message = "시리즈에 담을 글을 1개 이상 선택해주세요.") List<@NotNull(message = "글 ID를 확인해주세요.") Long> contentIds
) {
    public ReplaceBlogSeriesContentsCommand toCommand(Long seriesId) {
        return ReplaceBlogSeriesContentsCommand.of(seriesId, contentIds);
    }
}
