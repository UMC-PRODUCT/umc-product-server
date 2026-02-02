package com.umc.product.schedule.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.dto.request.CreateScheduleRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.CreateScheduleWithAttendanceRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.UpdateScheduleRequest;
import com.umc.product.schedule.application.port.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.CreateScheduleWithAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.DeleteScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.DeleteScheduleWithAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.UpdateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController implements ScheduleControllerApi {

    private final CreateScheduleUseCase createScheduleUseCase;
    private final CreateScheduleWithAttendanceUseCase createScheduleWithAttendanceUseCase;
    private final UpdateScheduleUseCase updateScheduleUseCase;
    private final DeleteScheduleUseCase deleteScheduleUseCase;
    private final DeleteScheduleWithAttendanceUseCase deleteScheduleWithAttendanceUseCase;

//    private final GetScheduleListUseCase getScheduleListUseCase;
//    private final GetAttendanceSheetUseCase getAttendanceSheetUseCase;
//    private final GetPendingAttendancesUseCase getPendingAttendancesUseCase;
//    private final CreateAttendanceSheetUseCase createAttendanceSheetUseCase;
//    private final UpdateAttendanceSheetUseCase updateAttendanceSheetUseCase;
//
//    @Override
//    @GetMapping
//    public ApiResponse<List<ScheduleListResponse>> getScheduleList() {
//        List<ScheduleListResponse> response = getScheduleListUseCase.getAll()
//                .stream()
//                .map(ScheduleListResponse::from)
//                .toList();
//        return ApiResponse.onSuccess(response);
//    }
//
//    @Override
//    @PostMapping("/{scheduleId}/attendance-sheets")
//    public ApiResponse<Long> createAttendanceSheet(
//            @PathVariable Long scheduleId,
//            @Valid @RequestBody CreateAttendanceSheetRequest request
//    ) {
//        AttendanceSheetId sheetId = createAttendanceSheetUseCase.create(request.toCommand(scheduleId));
//        return ApiResponse.onSuccess(sheetId.id());
//    }
//
//    @Override
//    @GetMapping("/{scheduleId}/attendance-sheets")
//    public ApiResponse<AttendanceSheetResponse> getAttendanceSheetBySchedule(
//            @PathVariable Long scheduleId
//    ) {
//        AttendanceSheetResponse response = AttendanceSheetResponse.from(
//                getAttendanceSheetUseCase.getByScheduleId(scheduleId)
//        );
//        return ApiResponse.onSuccess(response);
//    }
//
//    @Override
//    @PutMapping("/attendance-sheets/{sheetId}")
//    public ApiResponse<Void> updateAttendanceSheet(
//            @PathVariable Long sheetId,
//            @Valid @RequestBody UpdateAttendanceSheetRequest request
//    ) {
//        updateAttendanceSheetUseCase.update(request.toCommand(sheetId));
//        return ApiResponse.onSuccess(null);
//    }
//
//    @Override
//    @DeleteMapping("/attendance-sheets/{sheetId}")
//    public ApiResponse<Void> deactivateAttendanceSheet(@PathVariable Long sheetId) {
//        updateAttendanceSheetUseCase.deactivate(new AttendanceSheetId(sheetId));
//        return ApiResponse.onSuccess(null);
//    }
//
//    @Override
//    @PostMapping("/attendance-sheets/{sheetId}/activate")
//    public ApiResponse<Void> activateAttendanceSheet(@PathVariable Long sheetId) {
//        updateAttendanceSheetUseCase.activate(new AttendanceSheetId(sheetId));
//        return ApiResponse.onSuccess(null);
//    }
//
//    @Override
//    @GetMapping("/{scheduleId}/pending-attendances")
//    public ApiResponse<List<PendingAttendanceResponse>> getPendingAttendances(
//            @PathVariable Long scheduleId
//    ) {
//        List<PendingAttendanceResponse> response = getPendingAttendancesUseCase.getPendingList(scheduleId)
//                .stream()
//                .map(PendingAttendanceResponse::from)
//                .toList();
//        return ApiResponse.onSuccess(response);
//    }

    @Override
    @PostMapping
    public void createSchedule(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateScheduleRequest request
    ) {
        CreateScheduleCommand command = request.toCommand(memberPrincipal.getMemberId());
        createScheduleUseCase.create(command);
    }

    @Override
    @PostMapping("/with-attendance")
    public Long createScheduleWithAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateScheduleWithAttendanceRequest request
    ) {
        return createScheduleWithAttendanceUseCase.create(
            request.toCommand(memberPrincipal.getMemberId())
        );
    }

    @Override
    @PatchMapping("/{scheduleId}")
    public void updateSchedule(
        @PathVariable Long scheduleId,
        @RequestBody UpdateScheduleRequest request
    ) {
        updateScheduleUseCase.update(request.toCommand(scheduleId));
    }

    @Override
    @DeleteMapping("/{scheduleId}")
    public void deleteSchedule(@PathVariable Long scheduleId) {
        deleteScheduleUseCase.delete(scheduleId);
    }

    @Override
    @DeleteMapping("/{scheduleId}/with-attendance")
    public void deleteScheduleWithAttendance(@PathVariable Long scheduleId) {
        deleteScheduleWithAttendanceUseCase.delete(scheduleId);
    }
}
