package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.PartQuotaStatus;

/**
 * 프로젝트 파트별 TO 정보를 담는 Info DTO. {@code status}는 {@code quota}/{@code currentCount}로부터
 * 결정되는 파생값으로, 정적 팩토리 {@link #of}에서 자동 채워집니다.
 */
public record ProjectPartQuotaInfo(
    ChallengerPart part,
    long quota,
    long currentCount,
    PartQuotaStatus status
) {
    public static ProjectPartQuotaInfo of(ChallengerPart part, long quota, long currentCount) {
        PartQuotaStatus status = currentCount < quota
            ? PartQuotaStatus.RECRUITING
            : PartQuotaStatus.COMPLETED;
        return new ProjectPartQuotaInfo(part, quota, currentCount, status);
    }
}
