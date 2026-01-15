package com.umc.product.notice.dto;

import com.umc.product.challenger.domain.ChallengerRole;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;

/*
 * Notice 내 공용 DTO - 공지 작성자 정보
 * command에서 분리해서 공지사항 작성자 정보를 따로 관리합니다
 */
public record NoticeAuthorInfo(
        Long challengerId,
        ChallengerRoleType roleType,
        OrganizationType organizationType,
        Long organizationId,
        Long gisuId,
        ChallengerPart leadingPart
) {

    public static NoticeAuthorInfo from(Long challengerId, ChallengerRole role) {
        return new NoticeAuthorInfo(
                challengerId,
                role.getChallengerRoleType(),
                role.getOrganizationType(),
                role.getOrganizationId(),
                role.getGisuId(),
                role.getLeadingPart()
        );
    }
}
