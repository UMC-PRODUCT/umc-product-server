package com.umc.product.challenger.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;

/**
 * 챌린저 정보를 변경할 때 공통으로 사용합니다.
 *
 * @param challengerId
 * @param newPart
 * @param newStatus
 */
public record UpdateChallengerCommand(
        Long challengerId,
        ChallengerPart newPart,
        ChallengerStatus newStatus,
        String reason,
        Long modifiedBy
) {
    // 파트 변경용
    public static UpdateChallengerCommand forPartChange(
            Long challengerId,
            ChallengerPart newPart,
            Long modifiedBy) {
        return new UpdateChallengerCommand(challengerId, newPart, null, null, modifiedBy);
    }

    // 상태 변경용
    public static UpdateChallengerCommand forStatusChange(
            Long challengerId,
            ChallengerStatus newStatus,
            String reason,
            Long modifiedBy) {
        return new UpdateChallengerCommand(challengerId, null, newStatus, reason, modifiedBy);
    }
}
