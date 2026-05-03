package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import java.time.Instant;
import lombok.Builder;

/**
 * ProjectApplication 라이프사이클 스냅샷.
 * <p>
 * Create / Update / Submit UseCase 의 통일 반환 타입. Web 응답 record 가 이걸 받아 변환한다.
 * {@code lastSavedAt} 은 Survey FormResponse 의 값 — ProjectApplication 엔티티에는 없으므로 Service 가 조립한다.
 */
@Builder
public record ProjectApplicationInfo(
    Long applicationId,
    ProjectApplicationStatus status,
    Instant lastSavedAt
) {
}
