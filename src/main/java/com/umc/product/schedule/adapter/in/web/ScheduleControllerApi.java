package com.umc.product.schedule.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.dto.request.CreateScheduleRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.CreateScheduleWithAttendanceRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.UpdateScheduleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = SwaggerTag.Constants.SCHEDULE)
public interface ScheduleControllerApi {

//    @Operation(summary = "일정 목록 조회", description = "출석 통계와 함께 일정 목록을 조회합니다")
//    ApiResponse<List<ScheduleListResponse>> getScheduleList();
//
//    @Operation(summary = "출석부 생성", description = "일정에 대한 출석부를 생성합니다")
//    ApiResponse<Long> createAttendanceSheet(
//            @Parameter(description = "일정 ID") Long scheduleId,
//            CreateAttendanceSheetRequest request
//    );
//
//    @Operation(summary = "일정별 출석부 조회", description = "일정에 대한 출석부를 조회합니다")
//    ApiResponse<AttendanceSheetResponse> getAttendanceSheetBySchedule(
//            @Parameter(description = "일정 ID") Long scheduleId
//    );
//
//    @Operation(summary = "출석부 수정", description = "출석부 설정을 수정합니다")
//    ApiResponse<Void> updateAttendanceSheet(
//            @Parameter(description = "출석부 ID") Long sheetId,
//            UpdateAttendanceSheetRequest request
//    );
//
//    @Operation(summary = "출석부 비활성화", description = "출석부를 비활성화합니다")
//    ApiResponse<Void> deactivateAttendanceSheet(
//            @Parameter(description = "출석부 ID") Long sheetId
//    );
//
//    @Operation(summary = "출석부 활성화", description = "비활성화된 출석부를 다시 활성화합니다")
//    ApiResponse<Void> activateAttendanceSheet(
//            @Parameter(description = "출석부 ID") Long sheetId
//    );
//
//    @Operation(summary = "승인 대기 출석 조회", description = "승인 대기 중인 출석 요청 목록을 조회합니다")
//    ApiResponse<List<PendingAttendanceResponse>> getPendingAttendances(
//            @Parameter(description = "일정 ID") Long scheduleId
//    );

    @Operation(summary = "일정 단독 생성", description = "새로운 일정을 생성합니다")
    void createSchedule(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateScheduleRequest request
    );

    @Operation(summary = "일정 + 출석부 통합 생성", description = "일정과 출석부를 함께 생성합니다")
    Long createScheduleWithAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateScheduleWithAttendanceRequest request
    );

    @Operation(summary = "일정 수정", description = "일정 정보를 부분 수정합니다 (변경할 필드만 전송)")
    void updateSchedule(
        @Parameter(description = "일정 ID") @PathVariable Long scheduleId,
        @RequestBody UpdateScheduleRequest request
    );

    @Operation(summary = "일정 삭제", description = "일정을 삭제합니다")
    void deleteSchedule(
        @Parameter(description = "일정 ID") @PathVariable Long scheduleId
    );

    @Operation(summary = "일정 + 출석부 통합 삭제", description = "일정과 연결된 출석부를 함께 삭제합니다")
    void deleteScheduleWithAttendance(
        @Parameter(description = "일정 ID") @PathVariable Long scheduleId
    );
}
