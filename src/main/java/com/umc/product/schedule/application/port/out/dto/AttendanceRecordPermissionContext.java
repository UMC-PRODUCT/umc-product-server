package com.umc.product.schedule.application.port.out.dto;

/**
 * AttendanceRecord 권한 평가에 필요한 컨텍스트 정보
 * <p>
 * record → sheet → schedule을 JOIN하여 한 번의 쿼리로 조회
 */
public record AttendanceRecordPermissionContext(
    Long recordId,
    Long recordMemberId,
    Long sheetId,
    Long gisuId,
    Long scheduleId,
    Long authorChallengerId
) {
}
