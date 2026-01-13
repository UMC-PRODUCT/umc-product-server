package com.umc.product.notice.dto;

import com.umc.product.challenger.domain.enums.ChallengerPart;
import com.umc.product.challenger.domain.enums.OrganizationType;
import com.umc.product.challenger.domain.enums.RoleType;
import java.util.List;

/*
 * Notice 내 공용 DTO - 공지 대상 정보 (실제 작성된 공지의 대상)
 * command에서 분리해서 공지사항 대상자 정보를 따로 관리합니다
 */
public record NoticeTargetInfo(
        OrganizationType scope,
        Long organizationId,
        Long targetGisuId,
        List<RoleType> targetRoles,
        List<ChallengerPart> targetParts
) {

    /*
    * 리스트 미생성 상태인 경우 초기화
    * */
    public NoticeTargetInfo {
        targetRoles = targetRoles != null ? targetRoles : List.of();
        targetParts = targetParts != null ? targetParts : List.of();
    }
}
