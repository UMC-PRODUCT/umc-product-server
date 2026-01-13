package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import com.umc.product.schedule.domain.vo.AttendanceWindow;

/**
 * 출석부 정보 DTO
 */
public record AttendanceSheetInfo(
        AttendanceSheetId id,
        Long scheduleId,
        AttendanceWindow window,
        boolean requiresApproval,
        boolean active
) {
    public static AttendanceSheetInfo from(AttendanceSheet sheet) {
        return new AttendanceSheetInfo(
                sheet.getAttendanceSheetId(),
                sheet.getScheduleId(),
                sheet.getWindow(),
                sheet.isRequiresApproval(),
                sheet.isActive()
        );
    }
}
