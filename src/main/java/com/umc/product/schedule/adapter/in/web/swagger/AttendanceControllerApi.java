package com.umc.product.schedule.adapter.in.web.swagger;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.schedule.adapter.in.web.dto.request.CheckAttendanceRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.SubmitReasonRequest;
import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceRecordResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.AvailableAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.MyAttendanceHistoryResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.PendingAttendanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Schedule | 출석 체크", description = "")
public interface AttendanceControllerApi {

    @Operation(summary = "출석 체크", description = "현재 시간 기준으로 출석 체크를 수행합니다")
    Long checkAttendance(
        @Parameter(hidden = true) MemberPrincipal memberPrincipal,
        CheckAttendanceRequest request
    );

    @Operation(
        summary = "사유 제출 출석",
        description = "현장 도착 불가 시 사유를 제출하여 인정결석을 요청합니다. 관리자 승인 후 EXCUSED로 확정됩니다."
    )
    Long submitReasonAttendance(
        @Parameter(hidden = true) MemberPrincipal memberPrincipal,
        SubmitReasonRequest request
    );

    @Operation(summary = "출석 가능한 일정 조회", description = "현재 출석 가능한 일정 목록을 조회합니다")
    List<AvailableAttendanceResponse> getAvailableAttendances(
        @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "내 출석 이력 조회",
        description = """
            현재 활성 기수의 출석 이력을 조회합니다.

            ⚠️ 확정된 상태만 반환됩니다:
            - PRESENT: 출석 (인정결석 승인 시 포함)
            - LATE: 지각
            - ABSENT: 결석 (인정결석 거부 시 포함)

            제외되는 상태:
            - PENDING: 출석 전 (아직 체크 안함)
            - *_PENDING: 승인 대기 중 (관리자 승인 전)
            - EXCUSED: 표시 안 됨 (승인 시 PRESENT, 거부 시 ABSENT로 처리)
            """
    )
    List<MyAttendanceHistoryResponse> getMyAttendanceHistory(
        @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );

    @Operation(summary = "출석 기록 상세 조회", description = "출석 기록을 상세 조회합니다")
    AttendanceRecordResponse getAttendanceRecord(
        @Parameter(description = "출석 기록 ID") Long recordId
    );

    @Operation(summary = "승인 대기 출석 조회", description = "승인 대기 중인 출석 요청 목록을 조회합니다 (관리자)")
    List<PendingAttendanceResponse> getPendingAttendances(
        @Parameter(description = "일정 ID") Long scheduleId
    );

    @Operation(summary = "출석 승인", description = "승인 대기 중인 출석을 승인합니다 (관리자)")
    void approveAttendance(
        @Parameter(hidden = true) MemberPrincipal memberPrincipal,
        @Parameter(description = "출석 기록 ID") Long recordId
    );

    @Operation(summary = "출석 반려", description = "승인 대기 중인 출석을 반려합니다 (관리자)")
    void rejectAttendance(
        @Parameter(hidden = true) MemberPrincipal memberPrincipal,
        @Parameter(description = "출석 기록 ID") Long recordId
    );
}
