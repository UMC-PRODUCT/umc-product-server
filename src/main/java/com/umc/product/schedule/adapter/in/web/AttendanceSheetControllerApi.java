package com.umc.product.schedule.adapter.in.web;

import com.umc.product.schedule.adapter.in.web.dto.request.CreateAttendanceSheetRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.UpdateAttendanceSheetRequest;
import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceSheetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Attendance Sheet", description = "출석부 관리 API (관리자)")
public interface AttendanceSheetControllerApi {

    @Operation(summary = "출석부 조회", description = "일정에 대한 출석부를 조회합니다")
    AttendanceSheetResponse getAttendanceSheetBySchedule(
            @Parameter(description = "일정 ID") Long scheduleId
    );

    @Operation(summary = "출석부 생성", description = "일정에 대한 출석부를 생성합니다")
    Long createAttendanceSheet(
            @Parameter(description = "일정 ID") Long scheduleId,
            CreateAttendanceSheetRequest request
    );

    @Operation(summary = "출석부 수정", description = "출석부 설정을 수정합니다")
    void updateAttendanceSheet(
            @Parameter(description = "출석부 ID") Long sheetId,
            UpdateAttendanceSheetRequest request
    );

    @Operation(summary = "출석부 비활성화", description = "출석부를 비활성화합니다")
    void deactivateAttendanceSheet(
            @Parameter(description = "출석부 ID") Long sheetId
    );

    @Operation(summary = "출석부 활성화", description = "비활성화된 출석부를 다시 활성화합니다")
    void activateAttendanceSheet(
            @Parameter(description = "출석부 ID") Long sheetId
    );
}
