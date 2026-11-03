package com.umc.product.report.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.report.adapter.in.web.dto.ReportMemberReceiptResponse;
import com.umc.product.report.adapter.in.web.dto.ReportMemberRequest;
import com.umc.product.report.application.port.in.command.ReportMemberUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Validated
@Tag(name = "Report | 사용자 신고", description = "다른 사용자를 신고하는 API")
public class ReportMemberController {

    private final ReportMemberUseCase reportMemberUseCase;

    @PostMapping("/members/{memberId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        operationId = "REPORT-003",
        summary = "사용자 신고",
        description = "괴롭힘, 사칭, 스팸·광고 활동 등으로 다른 사용자를 신고합니다. 상세 내용은 필수이며 최대 500자예요. "
            + "신고자 정보와 내용은 신고 대상에게 공개되지 않아요. "
            + "대상 사용자가 없으면 MEMBER-0001, 자기 자신은 신고할 수 없고(REPORT-0005), "
            + "같은 사용자를 다시 신고할 수 없어요(COMMUNITY-0016)."
    )
    public ReportMemberReceiptResponse reportMember(
        @PathVariable @Positive Long memberId,
        @Valid @RequestBody ReportMemberRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        return ReportMemberReceiptResponse.from(reportMemberUseCase.report(
            request.toCommand(memberId, principal.getMemberId())
        ));
    }
}
