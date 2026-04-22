package com.umc.product.project.adapter.in.web.dto.common;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.ProjectPartQuotaInfo;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import lombok.Builder;

/**
 * 파트별 TO 정보 Web VO. Summary/Detail Response에 embedded.
 * <p>
 * Application layer의 {@link ProjectPartQuotaInfo}에 계산값 {@code status}를 추가한 형태.
 */
@Builder
public record PartQuotaInfo(
    ChallengerPart part,
    int currentCount,
    int quota,
    PartQuotaStatus status
) {
    public static PartQuotaInfo from(ProjectPartQuotaInfo info) {
        return PartQuotaInfo.builder()
            .part(info.part())
            .currentCount(info.currentCount())
            .quota(info.quota())
            .status(info.computeStatus())
            .build();
    }
}
