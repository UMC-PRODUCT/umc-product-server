package com.umc.product.schedule.application.port.in.command.dto;

/**
 * 출석 체크 처리 결과
 * <p>
 * 성공 시: success=true, recordId 포함
 * 실패 시: success=false, failureReason 포함 (시간 외, 비활성 출석부 등)
 */
public record CheckAttendanceResult(
    boolean success,
    Long recordId,
    String failureReason
) {
    public static CheckAttendanceResult success(Long recordId) {
        return new CheckAttendanceResult(true, recordId, null);
    }

    public static CheckAttendanceResult failure(String reason) {
        return new CheckAttendanceResult(false, null, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }
}
