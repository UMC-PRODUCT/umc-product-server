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
    @DeleteMapping("/{scheduleId}/with-attendance")
    public void deleteScheduleWithAttendance(@PathVariable Long scheduleId) {
        deleteScheduleWithAttendanceUseCase.delete(scheduleId);
    }
}
