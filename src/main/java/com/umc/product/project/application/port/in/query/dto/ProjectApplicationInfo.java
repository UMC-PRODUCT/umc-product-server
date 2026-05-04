package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import lombok.Builder;

/**
 * ProjectApplication 라이프사이클 스냅샷.
 * <p>
 * Create / Update / Submit UseCase 의 통일 반환 타입. Web 응답 record 가 이걸 받아 변환한다.
 */
@Builder
public record ProjectApplicationInfo(
    Long applicationId,
    ProjectApplicationStatus status
) {
    public static ProjectApplicationInfo of(
        Long applicationId,
        ProjectApplicationStatus status
    ) {
        return new ProjectApplicationInfo(applicationId, status);
    }
}
