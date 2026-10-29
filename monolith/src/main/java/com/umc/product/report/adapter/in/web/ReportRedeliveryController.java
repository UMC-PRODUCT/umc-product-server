package com.umc.product.report.adapter.in.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.report.adapter.in.web.dto.ReportRedeliveryResponse;
import com.umc.product.report.application.port.in.command.RedeliverReportsUseCase;
import com.umc.product.report.application.port.in.command.dto.RedeliverReportsCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "Report | 신고 운영 (어드민)", description = "SUPER_ADMIN이 신고 운영 채널 전달을 관리합니다.")
public class ReportRedeliveryController {

    private final RedeliverReportsUseCase redeliverReportsUseCase;

    @PostMapping("/redelivery")
    @Operation(
        operationId = "REPORT-101",
        summary = "미전달 신고 재전송",
        description = """
            운영 채널(Discord)로 전달하지 못한 신고를 오래된 순으로 최대 limit건 다시 보냅니다.
            실패(FAILED)했거나 한 번도 시도하지 않은 신고가 대상이며, 접수 후 5분이 지나지 않았거나 재시도 중인 신고는 제외합니다.
            SUPER_ADMIN 권한이 필요합니다.
            """
    )
    public ReportRedeliveryResponse redeliver(
        @RequestParam(defaultValue = "100") @Min(1) @Max(RedeliverReportsCommand.MAX_LIMIT) int limit,
        @CurrentMember MemberPrincipal principal
    ) {
        Long requesterId = principal == null ? null : principal.getMemberId();
        return ReportRedeliveryResponse.from(
            redeliverReportsUseCase.redeliver(new RedeliverReportsCommand(requesterId, limit))
        );
    }
}
