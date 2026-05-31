package com.umc.product.organization.application.port.in.query.dto.studygroup;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.time.Instant;

/**
 * 스터디 그룹 목록 조회 시 그룹 메타데이터만 담는 헤더 DTO.
 * <p>
 * 멤버/멘토 정보는 Service 가 별도 batch 조회로 합성한다.
 */
public record StudyGroupHeaderInfo(
    Long groupId,
    String name,
    Long gisuId,
    ChallengerPart part,
    Instant createdAt
) {
}
