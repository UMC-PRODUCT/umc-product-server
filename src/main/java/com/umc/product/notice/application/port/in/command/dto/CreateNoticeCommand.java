package com.umc.product.notice.application.port.in.command.dto;

import com.umc.product.challenger.domain.enums.ChallengerPart;
import com.umc.product.challenger.domain.enums.OrganizationType;
import com.umc.product.challenger.domain.enums.RoleType;
import java.util.List;

public record CreateNoticeCommand(
        Long authorChallengerId,
        OrganizationType scope, /* 공지 범위 (학교, 지부, 파트, 중앙운영진) */
        Long organizationId,
        String title,
        String content,
        Long targetGisuId,
        List<RoleType> targetRoles, /* 공지를 보여줄 대상의 role (전체, 운영진 공지 등) */
        List<ChallengerPart> targetParts, /* 공지를 보여줄 대상 파트들 (파트장은 선택 X) */
        Boolean shouldNotify /* 알림 발송 여부 */
) {
}
