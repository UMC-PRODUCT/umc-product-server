package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import lombok.Builder;

/**
 * 프로젝트 파트별 TO 정보를 담는 Info DTO.
 * <p>
 * Web 레이어의 PartQuotaInfo와는 분리되어 있습니다.
 * <ul>
 *   <li><b>Info (Application)</b>: raw 데이터 — part, quota, currentCount</li>
 *   <li><b>PartQuotaInfo (Web)</b>: 계산값 포함 — 위 필드 + {@link PartQuotaStatus status}</li>
 * </ul>
 */
@Builder
public record ProjectPartQuotaInfo(
    ChallengerPart part,
    int quota,        // 어드민이 PROJECT-105로 기록한 정원 (NOT NULL, 항상 ≥ 1)
    int currentCount  // 현재 등록된 활성 멤버 수 (실시간 조회)
) {
    /**
     * 현재 카운트와 정원을 비교해 모집 상태를 계산합니다.
     *
     * @return {@link PartQuotaStatus#RECRUITING} 혹은 {@link PartQuotaStatus#COMPLETED}
     */
    public PartQuotaStatus computeStatus() {
        return currentCount < quota
            ? PartQuotaStatus.RECRUITING
            : PartQuotaStatus.COMPLETED;
    }
}
