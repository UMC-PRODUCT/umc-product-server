package com.umc.product.schedule.domain.enums;

/**
 * 출석 상태를 나타내는 Enum.
 * <p>
 * 크게 3가지 계열로 나뉜다: - 확정 상태: PRESENT, LATE, ABSENT, EXCUSED - 최종 결정된 상태 - 승인 대기 상태(*_PENDING): 출석부의
 * requiresApproval=true일 때 관리자 승인을 기다리는 중간 상태 - 초기 상태(PENDING): 출석 체크 전 기본 상태
 * <p>
 * 상태 전이 흐름
 * <p>PENDING ──(체크인)──→ PRESENT / LATE
 * <p>(승인 불필요 시) PENDING ──(체크인)──→ PRESENT_PENDING / LATE_PENDING
 * <p>(승인 필요 시) *_PENDING ──(승인)──→ PRESENT / LATE / EXCUSED
 * <p>PENDING ──(반려)──→ ABSENT
 * <p>ABSENT ──(인정결석 신청)──→EXCUSED_PENDING
 */
public enum AttendanceStatus {
    PENDING(false),         // 출석 체크 전 초기 상태
    PRESENT(false),         // 출석 확정
    PRESENT_PENDING(true),  // 출석으로 체크했으나 관리자 승인 대기 중
    LATE(false),            // 지각 확정
    LATE_PENDING(true),     // 지각으로 체크했으나 관리자 승인 대기 중
    ABSENT(false),          // 결석 확정 (시간 초과 또는 승인 반려)
    EXCUSED(false),         // 인정결석 확정 (관리자 승인 완료)
    EXCUSED_PENDING(true);  // 인정결석 신청 후 관리자 승인 대기 중

    private final boolean isPending;

    AttendanceStatus(boolean isPending) {
        this.isPending = isPending;
    }

    public boolean isPending() {
        return this.isPending;
    }
}
