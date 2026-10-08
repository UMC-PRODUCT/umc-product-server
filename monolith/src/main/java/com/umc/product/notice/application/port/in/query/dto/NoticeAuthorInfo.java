package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;

public record NoticeAuthorInfo(
    Long memberId,
    String name,
    String nickname,
    Long gisuId,
    ChallengerRoleType roleType,
    String roleName,
    OrganizationType organizationType,
    Long organizationId,
    String organizationName
) {
}
