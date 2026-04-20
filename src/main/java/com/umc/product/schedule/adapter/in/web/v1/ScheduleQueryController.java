package com.umc.product.schedule.adapter.in.web.v1;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.MyScheduleResponse;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.ScheduleDetailResponse;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.ScheduleListResponse;
import com.umc.product.schedule.adapter.in.web.v1.swagger.ScheduleQueryControllerApi;
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

    @Override
    @GetMapping
    @CheckAccess(
        resourceType = ResourceType.SCHEDULE,
        permission = PermissionType.READ
    )
    public List<ScheduleListResponse> getScheduleList(
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        throw new NotImplementedException();
    }

    @Override
    @GetMapping("/my-list")
    public List<MyScheduleResponse> getMyScheduleList(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam int year,
        @RequestParam int month
    ) {
        throw new NotImplementedException();
    }

    @Override
    @GetMapping("/{scheduleId}")
    public ScheduleDetailResponse getScheduleDetail(
        @PathVariable Long scheduleId
    ) {
        throw new NotImplementedException();
    }
}
