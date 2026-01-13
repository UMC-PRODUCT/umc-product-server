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
public class ScheduleController implements ScheduleControllerApi {

    private final GetScheduleListUseCase getScheduleListUseCase;
    private final GetAttendanceSheetUseCase getAttendanceSheetUseCase;
    private final GetPendingAttendancesUseCase getPendingAttendancesUseCase;
    private final CreateAttendanceSheetUseCase createAttendanceSheetUseCase;
    private final UpdateAttendanceSheetUseCase updateAttendanceSheetUseCase;

    @Override
    @GetMapping
    public ApiResponse<List<ScheduleListResponse>> getScheduleList() {
        List<ScheduleListResponse> response = getScheduleListUseCase.getAll()
                .stream()
                .map(ScheduleListResponse::from)
                .toList();
        return ApiResponse.onSuccess(response);
    }

    @Override
    @PostMapping("/{scheduleId}/attendance-sheets")
    public ApiResponse<Long> createAttendanceSheet(
            @PathVariable Long scheduleId,
            @Valid @RequestBody CreateAttendanceSheetRequest request
    ) {
        AttendanceSheetId sheetId = createAttendanceSheetUseCase.create(request.toCommand(scheduleId));
        return ApiResponse.onSuccess(sheetId.id());
    }

    @Override
    @GetMapping("/{scheduleId}/attendance-sheets")
    public ApiResponse<AttendanceSheetResponse> getAttendanceSheetBySchedule(
            @PathVariable Long scheduleId
    ) {
        AttendanceSheetResponse response = AttendanceSheetResponse.from(
                getAttendanceSheetUseCase.getByScheduleId(scheduleId)
        );
        return ApiResponse.onSuccess(response);
    }

    @Override
    @PutMapping("/attendance-sheets/{sheetId}")
    public ApiResponse<Void> updateAttendanceSheet(
            @PathVariable Long sheetId,
            @Valid @RequestBody UpdateAttendanceSheetRequest request
    ) {
        updateAttendanceSheetUseCase.update(request.toCommand(sheetId));
        return ApiResponse.onSuccess(null);
    }

    @Override
    @DeleteMapping("/attendance-sheets/{sheetId}")
    public ApiResponse<Void> deactivateAttendanceSheet(@PathVariable Long sheetId) {
        updateAttendanceSheetUseCase.deactivate(new AttendanceSheetId(sheetId));
        return ApiResponse.onSuccess(null);
    }

    @Override
    @PostMapping("/attendance-sheets/{sheetId}/activate")
    public ApiResponse<Void> activateAttendanceSheet(@PathVariable Long sheetId) {
        updateAttendanceSheetUseCase.activate(new AttendanceSheetId(sheetId));
        return ApiResponse.onSuccess(null);
    }

    @Override
    @GetMapping("/{scheduleId}/pending-attendances")
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
