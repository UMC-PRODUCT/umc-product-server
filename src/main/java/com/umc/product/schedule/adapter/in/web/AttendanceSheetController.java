package com.umc.product.schedule.adapter.in.web;

import com.umc.product.schedule.adapter.in.web.dto.request.CreateAttendanceSheetRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.UpdateAttendanceSheetRequest;
import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceSheetResponse;
import com.umc.product.schedule.adapter.in.web.mapper.AttendanceSheetWebMapper;
import com.umc.product.schedule.application.port.in.command.CreateAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.command.UpdateAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.query.GetAttendanceSheetUseCase;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
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
public class AttendanceSheetController implements AttendanceSheetControllerApi {

    private final GetAttendanceSheetUseCase getAttendanceSheetUseCase;
    private final CreateAttendanceSheetUseCase createAttendanceSheetUseCase;
    private final UpdateAttendanceSheetUseCase updateAttendanceSheetUseCase;

    private final AttendanceSheetWebMapper mapper;

    @Override
    @GetMapping("/{scheduleId}/attendance-sheets")
    public AttendanceSheetResponse getAttendanceSheetBySchedule(
            @PathVariable Long scheduleId
    ) {
        return mapper.toAttendanceSheetResponse(
                getAttendanceSheetUseCase.getByScheduleId(scheduleId)
        );
    }

    @Override
    @PostMapping("/{scheduleId}/attendance-sheets")
    public Long createAttendanceSheet(
            @PathVariable Long scheduleId,
            @RequestBody CreateAttendanceSheetRequest request
    ) {
        return createAttendanceSheetUseCase.create(request.toCommand(scheduleId)).id();
    }

    @Override
    @PutMapping("/attendance-sheets/{sheetId}")
    public void updateAttendanceSheet(
            @PathVariable Long sheetId,
            @RequestBody UpdateAttendanceSheetRequest request
    ) {
        updateAttendanceSheetUseCase.update(request.toCommand(sheetId));
    }

    @Override
    @DeleteMapping("/attendance-sheets/{sheetId}")
    public void deactivateAttendanceSheet(@PathVariable Long sheetId) {
        updateAttendanceSheetUseCase.deactivate(new AttendanceSheetId(sheetId));
    }

    @Override
    @PostMapping("/attendance-sheets/{sheetId}/activate")
    public void activateAttendanceSheet(@PathVariable Long sheetId) {
        updateAttendanceSheetUseCase.activate(new AttendanceSheetId(sheetId));
    }
}
