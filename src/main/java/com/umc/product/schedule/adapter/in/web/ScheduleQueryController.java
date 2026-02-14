package com.umc.product.schedule.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.dto.response.MyScheduleResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleDetailResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleListResponse;
import com.umc.product.schedule.adapter.in.web.mapper.ScheduleWebMapper;
import com.umc.product.schedule.adapter.in.web.swagger.ScheduleQueryControllerApi;
import com.umc.product.schedule.application.port.in.query.GetMyScheduleUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleDetailUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleListUseCase;
import com.umc.product.schedule.application.port.in.query.dto.MyScheduleInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleDetailInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleQueryController implements ScheduleQueryControllerApi {

    private final GetScheduleListUseCase getScheduleListUseCase;
    private final GetMyScheduleUseCase getMyScheduleUseCase;
    private final GetScheduleDetailUseCase getScheduleDetailUseCase;
    private final ScheduleWebMapper mapper;

    @Override
    @GetMapping
    public List<ScheduleListResponse> getScheduleList() {
        return mapper.toScheduleListResponses(getScheduleListUseCase.getAll());
    }

    @Override
    @GetMapping("/my-list")
    public List<MyScheduleResponse> getMyScheduleList(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam int year,
        @RequestParam int month
    ) {
        List<MyScheduleInfo> infos = getMyScheduleUseCase.getMyMonthlySchedules(
            memberPrincipal.getMemberId(), year, month);

        return infos.stream()
            .map(MyScheduleResponse::from)
            .toList();
    }

    @Override
    @GetMapping("/{scheduleId}")
    public ScheduleDetailResponse getScheduleDetail(
        @PathVariable Long scheduleId
    ) {
        ScheduleDetailInfo info = getScheduleDetailUseCase.getScheduleDetail(scheduleId);
        return ScheduleDetailResponse.from(info);
    }
}
