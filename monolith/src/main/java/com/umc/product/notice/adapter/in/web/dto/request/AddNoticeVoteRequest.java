package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeVoteCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record AddNoticeVoteRequest(
    @Schema(description = "투표 제목")
    @NotBlank(message = "투표 제목은 필수입니다.")
    String title,

    @Schema(description = "익명 투표 여부")
    @NotNull(message = "익명 투표 여부는 필수입니다.")
    boolean isAnonymous,

    @Schema(description = "복수 선택 허용 여부 (false=단일선택, true=복수선택)")
    @NotNull(message = "복수 선택 허용 여부는 필수입니다.")
    boolean allowMultipleChoice,

    @Schema(description = "투표 시작 시각")
    @NotNull(message = "투표 시작 시각은 필수입니다.")
    Instant startsAt,

    @Schema(description = "투표 마감 시각 (exclusive)")
    @NotNull(message = "투표 마감 시각은 필수입니다.")
    Instant endsAtExclusive,

    @Schema(description = "투표 선택지 목록 (2~5개)")
    @NotEmpty(message = "투표 선택지는 필수입니다.")
    List<String> options
) {
    public AddNoticeVoteCommand toCommand(Long createdMemberId) {
        return new AddNoticeVoteCommand(
            createdMemberId,
            title,
            isAnonymous,
            allowMultipleChoice,
            startsAt,
            endsAtExclusive,
            options
        );
    }
}
