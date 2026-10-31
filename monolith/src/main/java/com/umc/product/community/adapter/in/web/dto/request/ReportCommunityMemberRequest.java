package com.umc.product.community.adapter.in.web.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umc.product.community.application.port.in.command.report.dto.ReportCommunityMemberCommand;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportTargetType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportCommunityMemberRequest(
    @Schema(description = "신고 사유 목록. 최소 1개이며 HARASSMENT(괴롭힘·지속적인 불쾌 행위), IMPERSONATION(사칭·허위 프로필), "
        + "SPAM(스팸·광고 활동), ETC(기타) 중에서 여러 개 고를 수 있습니다. 중복은 제거하고 고른 순서를 유지합니다.",
        example = "[\"HARASSMENT\", \"SPAM\"]")
    @NotEmpty List<@NotNull ReportReason> reasons,

    @Schema(description = "상세 신고 내용. 필수이며 공백만 입력할 수 없고 최대 500자", example = "반복해서 불쾌한 메시지를 보내요.")
    @NotBlank @Size(max = 500) String detail
) {

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "사용자 신고에 사용할 수 없는 사유예요.") public boolean isReasonsAllowed() {
        return reasons == null || reasons.stream()
            .allMatch(reason -> reason == null || reason.isAllowedFor(ReportTargetType.USER));
    }

    public ReportCommunityMemberCommand toCommand(Long targetMemberId, Long requesterMemberId) {
        return new ReportCommunityMemberCommand(targetMemberId, requesterMemberId, reasons, detail);
    }
}
