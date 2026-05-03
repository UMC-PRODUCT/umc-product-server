package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import lombok.Builder;

/**
 * 챌린저 지원서 라이프사이클 응답 (APPLY-001 / 002 / 003).
 * <p>
 * 이 호출 이후 application의 현재 상태.
 */
@Builder
public record ProjectApplicationStatusResponse(
    Long applicationId,
    ProjectApplicationStatus status
) {
}
