package com.umc.product.schedule.adapter.in.web;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.annotation.CurrentUser;
import com.umc.product.schedule.adapter.in.web.dto.request.CheckAttendanceRequest;
import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceRecordResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.AvailableAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.MyAttendanceHistoryResponse;
import com.umc.product.schedule.application.port.in.ApproveAttendanceUseCase;
import com.umc.product.schedule.application.port.in.CheckAttendanceUseCase;
import com.umc.product.schedule.application.port.in.query.GetAttendanceRecordUseCase;
import com.umc.product.schedule.application.port.in.query.GetAvailableAttendancesUseCase;
import com.umc.product.schedule.application.port.in.query.GetMyAttendanceHistoryUseCase;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "출석 체크 API (챌린저)")
public class AttendanceController {

    private final CheckAttendanceUseCase checkAttendanceUseCase;
    private final ApproveAttendanceUseCase approveAttendanceUseCase;
    private final GetAttendanceRecordUseCase getAttendanceRecordUseCase;
    private final GetAvailableAttendancesUseCase getAvailableAttendancesUseCase;
    private final GetMyAttendanceHistoryUseCase getMyAttendanceHistoryUseCase;

    @PostMapping("/check")
    @Operation(summary = "출석 체크", description = "현재 시간 기준으로 출석 체크를 수행합니다")
    public ApiResponse<Long> checkAttendance(
            @CurrentUser Long challengerId,
            @Valid @RequestBody CheckAttendanceRequest request
    ) {
        AttendanceRecordId recordId = checkAttendanceUseCase.check(request.toCommand(challengerId));
        return ApiResponse.onSuccess(recordId.id());
    }

    @GetMapping("/available")
    @Operation(summary = "출석 가능한 일정 조회", description = "현재 출석 가능한 일정 목록을 조회합니다")
    public ApiResponse<List<AvailableAttendanceResponse>> getAvailableAttendances(
            @CurrentUser Long challengerId
    ) {
        List<AvailableAttendanceResponse> response = getAvailableAttendancesUseCase.getAvailableList(challengerId)
                .stream()
                .map(AvailableAttendanceResponse::from)
                .toList();
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/history")
    @Operation(summary = "내 출석 이력 조회", description = "나의 출석 이력을 조회합니다")
    public ApiResponse<List<MyAttendanceHistoryResponse>> getMyAttendanceHistory(
            @CurrentUser Long challengerId
    ) {
        List<MyAttendanceHistoryResponse> response = getMyAttendanceHistoryUseCase.getHistory(challengerId)
                .stream()
                .map(MyAttendanceHistoryResponse::from)
                .toList();
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{recordId}")
    @Operation(summary = "출석 기록 상세 조회", description = "출석 기록을 상세 조회합니다")
    public ApiResponse<AttendanceRecordResponse> getAttendanceRecord(
            @PathVariable Long recordId
    ) {
        AttendanceRecordResponse response = AttendanceRecordResponse.from(
                getAttendanceRecordUseCase.getById(new AttendanceRecordId(recordId))
        );
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/{recordId}/approve")
    @Operation(summary = "출석 승인", description = "승인 대기 중인 출석을 승인합니다 (관리자)")
    public ApiResponse<Void> approveAttendance(
            @CurrentUser Long confirmerId,
            @PathVariable Long recordId
    ) {
        approveAttendanceUseCase.approve(new AttendanceRecordId(recordId), confirmerId);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/{recordId}/reject")
    @Operation(summary = "출석 반려", description = "승인 대기 중인 출석을 반려합니다 (관리자)")
    public ApiResponse<Void> rejectAttendance(
            @CurrentUser Long confirmerId,
            @PathVariable Long recordId
    ) {
        approveAttendanceUseCase.reject(new AttendanceRecordId(recordId), confirmerId);
        return ApiResponse.onSuccess(null);
    }
}
