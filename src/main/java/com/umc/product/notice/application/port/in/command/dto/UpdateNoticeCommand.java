package com.umc.product.notice.application.port.in.command.dto;

import com.umc.product.challenger.domain.enums.ChallengerPart;
import com.umc.product.challenger.domain.enums.RoleType;
import java.util.List;

public record UpdateNoticeCommand(
        Long noticeId,
        Long editorChallengerId, /* 수정자 ID (권한 확인용) */
        Long organizationId,
        String title,
        String content,
        List<RoleType> targetRoles, /* 수정된 대상 역할들 */
        List<ChallengerPart> targetParts, /* 수정된 대상 파트들 */
        Boolean shouldNotify /* 알림 발송 여부 */
) {
}
