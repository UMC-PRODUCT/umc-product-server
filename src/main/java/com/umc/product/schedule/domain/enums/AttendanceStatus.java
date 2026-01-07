package com.umc.product.schedule.domain.enums;

public enum AttendanceStatus {
    PENDING,          // 대기중
    PRESENT,          // 출석
    PRESENT_PENDING,  // 출석 승인 대기
    LATE,             // 지각
    LATE_PENDING,     // 지각 승인 대기
    ABSENT,           // 결석
    EXCUSED,          // 인정결석
    EXCUSED_PENDING   // 인정결석 승인 대기
}
