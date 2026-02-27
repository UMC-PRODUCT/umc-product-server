package com.umc.product.challenger.application.port.in.query.dto;

import com.umc.product.challenger.domain.ChallengerRecord;
import com.umc.product.common.domain.enums.ChallengerPart;
import java.time.Instant;
import lombok.Builder;

@Builder
public record ChallengerRecordInfo(
    Long id,
    String code,
    String memberName,
    Long createdMemberId,
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart part,
    boolean isUsed,
    Long usedMemberId,
    Instant usedAt
) {
    public static ChallengerRecordInfo from(ChallengerRecord entity) {
        return ChallengerRecordInfo.builder()
            .id(entity.getId())
            .code(entity.getCode())
            .memberName(entity.getMemberName())
            .createdMemberId(entity.getCreatedMemberId())
            .gisuId(entity.getGisuId())
            .chapterId(entity.getChapterId())
            .schoolId(entity.getSchoolId())
            .part(entity.getPart())
            .isUsed(entity.isUsed())
            .usedMemberId(entity.getUsedMemberId())
            .usedAt(entity.getUsedAt())
            .build();
    }
}
