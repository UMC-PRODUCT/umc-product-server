package com.umc.product.schedule.adapter.in.web;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.schedule.adapter.in.web.dto.request.CreateAttendanceSheetRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.UpdateAttendanceSheetRequest;
import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceSheetResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.PendingAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleListResponse;
import com.umc.product.schedule.application.port.in.CreateAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.UpdateAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.query.GetAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.query.GetPendingAttendancesUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleListUseCase;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "일정 및 출석부 관리 API (관리자)")
public class ScheduleController {

    private final GetScheduleListUseCase getScheduleListUseCase;
    private final GetAttendanceSheetUseCase getAttendanceSheetUseCase;
    private final GetPendingAttendancesUseCase getPendingAttendancesUseCase;
    private final CreateAttendanceSheetUseCase createAttendanceSheetUseCase;
    private final UpdateAttendanceSheetUseCase updateAttendanceSheetUseCase;

    @GetMapping
    @Operation(summary = "일정 목록 조회", description = "출석 통계와 함께 일정 목록을 조회합니다")
    public ApiResponse<List<ScheduleListResponse>> getScheduleList() {
        List<ScheduleListResponse> response = getScheduleListUseCase.getAll()
                .stream()
                .map(ScheduleListResponse::from)
                .toList();
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/{scheduleId}/attendance-sheets")
    @Operation(summary = "출석부 생성", description = "일정에 대한 출석부를 생성합니다")
    public ApiResponse<Long> createAttendanceSheet(
            @PathVariable Long scheduleId,
            @Valid @RequestBody CreateAttendanceSheetRequest request
    ) {
        AttendanceSheetId sheetId = createAttendanceSheetUseCase.create(request.toCommand());
        return ApiResponse.onSuccess(sheetId.id());
    }

    @GetMapping("/{scheduleId}/attendance-sheets")
    @Operation(summary = "일정별 출석부 조회", description = "일정에 대한 출석부를 조회합니다")
    public ApiResponse<AttendanceSheetResponse> getAttendanceSheetBySchedule(
            @PathVariable Long scheduleId
    ) {
        AttendanceSheetResponse response = AttendanceSheetResponse.from(
                getAttendanceSheetUseCase.getByScheduleId(scheduleId)
        );
        return ApiResponse.onSuccess(response);
    }

    @PutMapping("/attendance-sheets/{sheetId}")
    @Operation(summary = "출석부 수정", description = "출석부 설정을 수정합니다")
    public ApiResponse<Void> updateAttendanceSheet(
            @PathVariable Long sheetId,
            @Valid @RequestBody UpdateAttendanceSheetRequest request
    ) {
        updateAttendanceSheetUseCase.update(request.toCommand(sheetId));
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/attendance-sheets/{sheetId}")
    @Operation(summary = "출석부 비활성화", description = "출석부를 비활성화합니다")
    public ApiResponse<Void> deactivateAttendanceSheet(@PathVariable Long sheetId) {
        updateAttendanceSheetUseCase.deactivate(new AttendanceSheetId(sheetId));
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/attendance-sheets/{sheetId}/activate")
    @Operation(summary = "출석부 활성화", description = "비활성화된 출석부를 다시 활성화합니다")
    public ApiResponse<Void> activateAttendanceSheet(@PathVariable Long sheetId) {
        updateAttendanceSheetUseCase.activate(new AttendanceSheetId(sheetId));
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{scheduleId}/pending-attendances")
    @Operation(summary = "승인 대기 출석 조회", description = "승인 대기 중인 출석 요청 목록을 조회합니다")
    public ApiResponse<List<PendingAttendanceResponse>> getPendingAttendances(
            @PathVariable Long scheduleId
    ) {
        List<PendingAttendanceResponse> response = getPendingAttendancesUseCase.getPendingList(scheduleId)
                .stream()
                .map(PendingAttendanceResponse::from)
                .toList();
        return ApiResponse.onSuccess(response);
    }
}
