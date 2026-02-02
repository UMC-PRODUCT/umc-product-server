package com.umc.product.schedule.domain.vo;

/**
 * 출석 통계 Value Object. 전체/출석/대기 건수를 집계하고 출석률을 계산
 * <p>출석률 = (presentCount / totalCount) * 100, 소수점 첫째 자리 반올림용
 */
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
        return Math.round(rate * 10) / 10.0; // 소수점 첫째 자리 반올림을 위해 둠
    }
}
