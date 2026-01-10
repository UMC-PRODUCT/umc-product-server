package com.umc.product.schedule.application.port.in.attendance;

import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import com.umc.product.schedule.domain.vo.Location;
import com.umc.product.schedule.domain.vo.LocationRange;

//출석부 응답 DTO
public record AttendanceSheetResponse(
        AttendanceSheetId id,
        Long scheduleId,
        Location location,
        LocationRange range,
        AttendanceWindow window,
        boolean requiresApproval,
        boolean active
) {
    public static AttendanceSheetResponse from(AttendanceSheet sheet) {
        return new AttendanceSheetResponse(
                sheet.getAttendanceSheetId(),
                sheet.getScheduleId(),
                sheet.getLocation(),
                sheet.getRange(),
                sheet.getWindow(),
                sheet.isRequiresApproval(),
                sheet.isActive()
        );
    }
}
