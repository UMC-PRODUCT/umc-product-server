package com.umc.product.schedule.adapter.in.web;

import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleListResponse;
import com.umc.product.schedule.adapter.in.web.mapper.ScheduleWebMapper;
import com.umc.product.schedule.application.port.in.query.GetScheduleListUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleQueryController implements ScheduleQueryControllerApi {

    private final GetScheduleListUseCase getScheduleListUseCase;
    private final ScheduleWebMapper mapper;

    @Override
    @GetMapping
    public List<ScheduleListResponse> getScheduleList() {
        return mapper.toScheduleListResponses(getScheduleListUseCase.getAll());
    }
}
