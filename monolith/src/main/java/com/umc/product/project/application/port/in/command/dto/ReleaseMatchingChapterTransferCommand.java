package com.umc.product.project.application.port.in.command.dto;

import lombok.Builder;

/**
 * 매칭 지부 이동 해제 커맨드.
 *
 * @param requesterMemberId 해제하는 운영진 Member ID
 * @param transferId        해제할 이동 기록 ID
 * @param reason            해제 사유 (선택)
 */
@Builder
public record ReleaseMatchingChapterTransferCommand(
    Long requesterMemberId,
    Long transferId,
    String reason
) {
}
