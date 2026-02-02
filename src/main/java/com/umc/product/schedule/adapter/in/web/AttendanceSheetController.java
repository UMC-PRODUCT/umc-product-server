package com.umc.product.schedule.adapter.in.web;

import com.umc.product.schedule.adapter.in.web.dto.request.UpdateAttendanceSheetRequest;
import com.umc.product.schedule.application.port.in.command.UpdateAttendanceSheetUseCase;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    private final UpdateAttendanceSheetUseCase updateAttendanceSheetUseCase;

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
