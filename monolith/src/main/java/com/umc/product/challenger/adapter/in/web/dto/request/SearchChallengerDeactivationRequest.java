package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerDeactivationQuery;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationGroup;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationListStatus;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerPart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SearchChallengerDeactivationRequest(
    @Positive @Schema(description = "조회 기수 ID. 생략하면 활성 기수") Long gisuId,
    @Positive Long chapterId,
    @Positive Long schoolId,
    ChallengerPart part,
    @Size(max = 100) @Schema(description = "이름·닉네임·이메일 검색어") String keyword,
    ChallengerDeactivationReason reason,
    @Schema(description = "PENDING(등록 전), REGISTERED(탈부). 생략하면 전체")
    ChallengerDeactivationListStatus status,
    @Schema(description = "탈부 유형. VOLUNTARY(자진), INVOLUNTARY(타의). 생략하면 전체")
    ChallengerDeactivationGroup type
) {
    public SearchChallengerDeactivationQuery toQuery() {
        return new SearchChallengerDeactivationQuery(gisuId, chapterId, schoolId, part, keyword, reason, status, type);
    }
}
