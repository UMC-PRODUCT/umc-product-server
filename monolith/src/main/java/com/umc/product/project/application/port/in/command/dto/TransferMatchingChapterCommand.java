package com.umc.product.project.application.port.in.command.dto;

import java.util.List;

import lombok.Builder;

/**
 * 매칭 지부 이동 일괄 등록 커맨드.
 *
 * @param requesterMemberId 등록하는 운영진 Member ID
 * @param gisuId            이동을 적용할 기수
 * @param targets           이동 대상 목록
 */
@Builder
public record TransferMatchingChapterCommand(
    Long requesterMemberId,
    Long gisuId,
    List<Target> targets
) {

    /**
     * @param memberId    이동 대상 Member ID
     * @param toChapterId 이동지부 ID
     * @param reason      이동 사유 (선택)
     */
    public record Target(Long memberId, Long toChapterId, String reason) {
    }
}
