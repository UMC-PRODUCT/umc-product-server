package com.umc.product.organization.adapter.in.web;

import com.umc.product.organization.adapter.in.web.swagger.StudyGroupScheduleControllerApi;
import com.umc.product.organization.application.port.in.command.CreateStudyGroupScheduleUseCase;
import com.umc.product.schedule.adapter.in.web.v1.dto.request.CreateStudyGroupScheduleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/study-group-schedules")
@RequiredArgsConstructor
public class StudyGroupScheduleController implements StudyGroupScheduleControllerApi {

    private final CreateStudyGroupScheduleUseCase createStudyGroupScheduleUseCase;

    // 스터디 그룹 일정 생성
    @Override
    @PostMapping
    public Long create(@Valid @RequestBody CreateStudyGroupScheduleRequest request) {
        return createStudyGroupScheduleUseCase.create(request.toCommand());
    }
}
