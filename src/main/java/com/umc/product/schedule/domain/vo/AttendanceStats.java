package com.umc.product.schedule.domain.vo;

public record AttendanceStats(
        int totalCount,
        int presentCount,
        int pendingCount
) {
    public double calculateAttendanceRate() {
        if (totalCount <= 0) {
            return 0.0;
        }
        double rate = (presentCount * 100.0) / totalCount;
        return Math.round(rate * 10) / 10.0; // 소수점 첫째 자리 반올림
    }
}