package com.umc.product.test.application.port.in.command.dto;

import java.util.List;

/**
 * Notice 시딩 결과. ADR-017 참조.
 *
 * @param gisuId           시딩 대상 기수
 * @param authorMemberId   공지 작성자 멤버 ID
 * @param createdNoticeIds 생성된 Notice ID 목록 (모든 scope 의 합집합)
 * @param scopeBreakdown   scope (GLOBAL/CHAPTER/SCHOOL/PART) 별 시도·성공·실패 수
 * @param totalCreated     생성 합계
 * @param totalFailed      실패 합계 (권한 부족·검증 실패 등)
 * @param skipped          전체 스킵 여부 (활성 기수 부재 등)
 * @param reason           스킵/부분 실패 사유 (skipped=false 이고 실패 없으면 null)
 */
public record SeedNoticeResult(
    Long gisuId,
    Long authorMemberId,
    List<Long> createdNoticeIds,
    List<ScopeSummary> scopeBreakdown,
    int totalCreated,
    int totalFailed,
    boolean skipped,
    String reason
) {

    /**
     * scope 별 시딩 결과.
     *
     * @param scope     "GLOBAL" / "CHAPTER" / "SCHOOL" / "PART" 중 하나
     * @param attempted 시도한 공지 수
     * @param created   생성된 공지 수
     * @param failed    실패한 공지 수 (권한 부족·도메인 검증 등)
     */
    public record ScopeSummary(
        String scope,
        int attempted,
        int created,
        int failed
    ) {
    }

    public static SeedNoticeResult skipped(Long gisuId, Long authorMemberId, String reason) {
        return new SeedNoticeResult(gisuId, authorMemberId, List.of(), List.of(), 0, 0, true, reason);
    }
}
