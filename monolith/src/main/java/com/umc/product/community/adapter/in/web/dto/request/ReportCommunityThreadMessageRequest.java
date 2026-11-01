package com.umc.product.community.adapter.in.web.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umc.product.community.application.port.in.command.thread.report.dto.ReportCommunityThreadMessageCommand;
import com.umc.product.report.domain.enums.ReportReason;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ReportCommunityThreadMessageRequest(
    @Schema(description = "신고 사유 목록. 여러 개 고를 수 있으며 중복은 제거하고 고른 순서를 유지합니다. "
        + "reasons와 reason 중 하나는 반드시 보내야 합니다.", example = "[\"SPAM\", \"ABUSE\"]")
    List<@NotNull ReportReason> reasons,

    @Deprecated
    @Schema(description = "신고 사유 하나. 지원 중단 예정이니 reasons를 사용하세요. reasons가 있으면 무시합니다.",
        deprecated = true, example = "SPAM")
    ReportReason reason
) {

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "신고 사유(reasons)를 한 개 이상 보내 주세요.") public boolean isReasonProvided() {
        return !selectedReasons().isEmpty();
    }

    public ReportCommunityThreadMessageCommand toCommand(Long messageId, Long requesterMemberId) {
        return new ReportCommunityThreadMessageCommand(messageId, requesterMemberId, selectedReasons());
    }

    /** reasons가 있으면 reasons를, 없으면 예전 단일 reason을 목록으로 바꿔 씁니다. */
    private List<ReportReason> selectedReasons() {
        if (reasons != null && !reasons.isEmpty()) {
            return reasons;
        }
        return reason == null ? List.of() : List.of(reason);
    }
}
