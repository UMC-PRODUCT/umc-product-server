package com.umc.product.community.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.community.adapter.in.web.dto.request.ReportCommunityMemberRequest;
import com.umc.product.community.adapter.in.web.dto.response.CommunityMemberReportReceiptResponse;
import com.umc.product.community.adapter.in.web.validation.PositiveDecimalId;
import com.umc.product.community.application.port.in.command.report.ReportCommunityMemberUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
@Validated
@Tag(name = "Community | Member Report", description = "사용자 신고 API")
public class CommunityMemberReportController {

    private final ReportCommunityMemberUseCase reportMemberUseCase;

    @PostMapping("/members/{memberId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        operationId = "COMMUNITY-MEMBER-REPORT-001",
        summary = "사용자 신고",
        description = "괴롭힘, 사칭, 스팸·광고 활동 등으로 다른 사용자를 신고합니다. 상세 내용은 필수이며 최대 500자예요. "
            + "신고자 정보와 내용은 신고 대상에게 공개되지 않아요. "
            + "자기 자신은 신고할 수 없고(REPORT-0005), 같은 사용자를 다시 신고할 수 없어요(COMMUNITY-0016)."
    )
    public CommunityMemberReportReceiptResponse reportMember(
        @PathVariable @PositiveDecimalId String memberId,
        @Valid @RequestBody ReportCommunityMemberRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        return CommunityMemberReportReceiptResponse.from(reportMemberUseCase.report(
            request.toCommand(CommunityWebNumbers.id(memberId), principal.getMemberId())
        ));
    }
}
