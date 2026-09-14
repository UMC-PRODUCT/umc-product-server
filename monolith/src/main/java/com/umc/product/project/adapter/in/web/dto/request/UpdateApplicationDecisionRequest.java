package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.project.application.port.in.command.dto.ApplicationDecisionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * APPLY-103 — PM 의 합/불 결정 요청.
 * <p>
 * {@code reason} 은 nullable. 현재 UI 에 입력 칸은 없으나 도메인/DB 인프라가 이미 받게 되어 있어
 * 향후 사유 입력이 추가되거나 자동 매칭에서 시스템 메시지가 들어갈 수 있도록 열어둔다.
 */
public record UpdateApplicationDecisionRequest(
    @NotNull(message = "결정 상태는 필수입니다.")
    ApplicationDecisionStatus status,

    @Size(max = 500, message = "결정 사유는 500자 이내여야 합니다.")
    String reason
) {}
