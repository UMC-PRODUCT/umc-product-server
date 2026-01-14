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
public class AttendanceController implements AttendanceControllerApi {

    private final CheckAttendanceUseCase checkAttendanceUseCase;
    private final ApproveAttendanceUseCase approveAttendanceUseCase;
    private final GetAttendanceRecordUseCase getAttendanceRecordUseCase;
    private final GetAvailableAttendancesUseCase getAvailableAttendancesUseCase;
    private final GetMyAttendanceHistoryUseCase getMyAttendanceHistoryUseCase;

    @Override
    @PostMapping("/check")
    public ApiResponse<Long> checkAttendance(
            @CurrentUser Long challengerId,
            @Valid @RequestBody CheckAttendanceRequest request
    ) {
        AttendanceRecordId recordId = checkAttendanceUseCase.check(request.toCommand(challengerId));
        return ApiResponse.onSuccess(recordId.id());
    }

    @Override
    @GetMapping("/available")
    public ApiResponse<List<AvailableAttendanceResponse>> getAvailableAttendances(
            @CurrentUser Long challengerId
    ) {
        List<AvailableAttendanceResponse> response = getAvailableAttendancesUseCase.getAvailableList(challengerId)
                .stream()
                .map(AvailableAttendanceResponse::from)
                .toList();
        return ApiResponse.onSuccess(response);
    }

    @Override
    @GetMapping("/history")
    public ApiResponse<List<MyAttendanceHistoryResponse>> getMyAttendanceHistory(
            @CurrentUser Long challengerId
    ) {
        List<MyAttendanceHistoryResponse> response = getMyAttendanceHistoryUseCase.getHistory(challengerId)
                .stream()
                .map(MyAttendanceHistoryResponse::from)
                .toList();
        return ApiResponse.onSuccess(response);
    }

    @Override
    @GetMapping("/{recordId}")
    public ApiResponse<AttendanceRecordResponse> getAttendanceRecord(
            @PathVariable Long recordId
    ) {
        AttendanceRecordResponse response = AttendanceRecordResponse.from(
                getAttendanceRecordUseCase.getById(new AttendanceRecordId(recordId))
        );
        return ApiResponse.onSuccess(response);
    }

    @Override
    @PostMapping("/{recordId}/approve")
    public ApiResponse<Void> approveAttendance(
            @CurrentUser Long confirmerId,
            @PathVariable Long recordId
    ) {
        approveAttendanceUseCase.approve(new AttendanceRecordId(recordId), confirmerId);
        return ApiResponse.onSuccess(null);
    }

    @Override
    @PostMapping("/{recordId}/reject")
    public ApiResponse<Void> rejectAttendance(
            @CurrentUser Long confirmerId,
            @PathVariable Long recordId
    ) {
        approveAttendanceUseCase.reject(new AttendanceRecordId(recordId), confirmerId);
        return ApiResponse.onSuccess(null);
    }
}
