package com.umc.product.organization.adapter.out.persistence.studygroup.projection;

import com.umc.product.common.domain.enums.ChallengerPart;

/**
 * {@link #fetchGroupHeaders} 용 상위 그룹 헤더 Projection.
 *
 * @param groupId 스터디 그룹 ID
 * @param name    스터디 그룹 이름
 */
public record StudyGroupHeaderRow(
    Long groupId, String name,
    Long gisuId, ChallengerPart part
) {
}
