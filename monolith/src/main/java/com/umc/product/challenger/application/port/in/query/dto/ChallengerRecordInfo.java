package com.umc.product.challenger.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.challenger.domain.ChallengerRecord;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

import lombok.Builder;

@Builder
public record ChallengerRecordInfo(
    Long id,
    String code,
    String memberName,
    ChallengerRoleType challengerRoleType,
    Long organizationId,
    Long createdMemberId,
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart part,
    boolean infra,
    boolean isUsed,
    Long usedMemberId,
    Instant usedAt
) {
    public static ChallengerRecordInfo from(ChallengerRecord entity) {
        return ChallengerRecordInfo.builder()
            .id(entity.getId())
            .code(entity.getCode())
            .memberName(entity.getMemberName())
            .challengerRoleType(entity.getChallengerRoleType())
            .organizationId(entity.getOrganizationId())
            .createdMemberId(entity.getCreatedMemberId())
            .gisuId(entity.getGisuId())
            .chapterId(entity.getChapterId())
            .schoolId(entity.getSchoolId())
            .part(entity.getPart())
            .infra(entity.isInfra())
            .isUsed(entity.isUsed())
            .usedMemberId(entity.getUsedMemberId())
            .usedAt(entity.getUsedAt())
            .build();
    }
}
