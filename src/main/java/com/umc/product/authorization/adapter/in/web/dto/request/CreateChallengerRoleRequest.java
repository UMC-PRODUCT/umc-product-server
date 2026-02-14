package com.umc.product.authorization.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

/**
 * @param roleType        역할의 유형이며, 이에 따라서 `organizationId`가 어떤 조직의 ID값을 뜻하는지 결정합니다.
 * @param organizationId  see {@link com.umc.product.authorization.domain.ChallengerRole}의 `update` 메서드를 참고하시면 됩니다.
 * @param responsiblePart
 * @param gisuId
 */
public record CreateChallengerRoleRequest(
    Long challengerId,
    ChallengerRoleType roleType,
    Long organizationId,
    ChallengerPart responsiblePart,
    Long gisuId
) {
}
