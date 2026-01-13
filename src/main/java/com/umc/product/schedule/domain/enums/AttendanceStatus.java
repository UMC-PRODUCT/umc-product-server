package com.umc.product.schedule.domain.enums;

public enum AttendanceStatus {
    PENDING(false),         // 대기중
    PRESENT(false),         // 출석
    PRESENT_PENDING(true),  // 출석 승인 대기
    LATE(false),            // 지각
    LATE_PENDING(true),     // 지각 승인 대기
    ABSENT(false),          // 결석
    EXCUSED(false),         // 인정결석
    EXCUSED_PENDING(true);  // 인정결석 승인 대기

    private final boolean isPending;

    AttendanceStatus(boolean isPending) {
        this.isPending = isPending;
    }

    public boolean isPending() {
        return this.isPending;
    }
}

