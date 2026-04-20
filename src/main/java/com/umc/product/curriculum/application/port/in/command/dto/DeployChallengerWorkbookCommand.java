package com.umc.product.curriculum.application.port.in.command.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

/**
 * 챌린저 워크북 배포 요청 커맨드
 *
 * @param originalWorkbookIds 배포받을 원본 워크북 ID 목록 (한 번에 여러 개 배포 가능)
 * @param requesterMemberId   요청 사용자의 멤버 ID
 */
@Builder
public record DeployChallengerWorkbookCommand(
    @NotEmpty(message = "배포할 원본 워크북 ID는 하나 이상이어야 합니다")
    List<Long> originalWorkbookIds,

    @NotNull(message = "멤버 ID는 필수입니다")
    Long requesterMemberId
) {
}