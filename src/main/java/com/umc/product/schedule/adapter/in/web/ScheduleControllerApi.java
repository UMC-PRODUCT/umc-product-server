package com.umc.product.schedule.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.schedule.adapter.in.web.dto.request.CreateAttendanceSheetRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.UpdateAttendanceSheetRequest;
import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceSheetResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.PendingAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = Constants.SCHEDULE)
public interface ScheduleControllerApi {

    @Operation(summary = "일정 목록 조회", description = "출석 통계와 함께 일정 목록을 조회합니다")
    ApiResponse<List<ScheduleListResponse>> getScheduleList();

    @Operation(summary = "출석부 생성", description = "일정에 대한 출석부를 생성합니다")
    ApiResponse<Long> createAttendanceSheet(
            @Parameter(description = "일정 ID") Long scheduleId,
            CreateAttendanceSheetRequest request
    );

    @Operation(summary = "일정별 출석부 조회", description = "일정에 대한 출석부를 조회합니다")
    ApiResponse<AttendanceSheetResponse> getAttendanceSheetBySchedule(
            @Parameter(description = "일정 ID") Long scheduleId
    );

    @Operation(summary = "출석부 수정", description = "출석부 설정을 수정합니다")
    ApiResponse<Void> updateAttendanceSheet(
            @Parameter(description = "출석부 ID") Long sheetId,
            UpdateAttendanceSheetRequest request
    );

    @Operation(summary = "출석부 비활성화", description = "출석부를 비활성화합니다")
    ApiResponse<Void> deactivateAttendanceSheet(
            @Parameter(description = "출석부 ID") Long sheetId
    );

    @Operation(summary = "출석부 활성화", description = "비활성화된 출석부를 다시 활성화합니다")
    ApiResponse<Void> activateAttendanceSheet(
            @Parameter(description = "출석부 ID") Long sheetId
    );

    @Operation(summary = "승인 대기 출석 조회", description = "승인 대기 중인 출석 요청 목록을 조회합니다")
    ApiResponse<List<PendingAttendanceResponse>> getPendingAttendances(
            @Parameter(description = "일정 ID") Long scheduleId
    );
}
