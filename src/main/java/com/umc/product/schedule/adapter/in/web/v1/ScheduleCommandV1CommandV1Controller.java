package com.umc.product.schedule.adapter.in.web.v1;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.adapter.in.web.dto.request.CreateStudyGroupScheduleRequest;
import com.umc.product.schedule.adapter.in.web.v1.dto.request.CreateScheduleRequest;
import com.umc.product.schedule.adapter.in.web.v1.dto.request.CreateScheduleWithAttendanceRequest;
import com.umc.product.schedule.adapter.in.web.v1.dto.request.UpdateScheduleLocationRequest;
import com.umc.product.schedule.adapter.in.web.v1.dto.request.UpdateScheduleRequest;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.UpdateScheduleLocationResponse;
import com.umc.product.schedule.adapter.in.web.v1.swagger.ScheduleCommandV1ControllerApi;
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
@Deprecated
public class ScheduleCommandV1CommandV1Controller implements ScheduleCommandV1ControllerApi {

    @Override
    @PostMapping
    @CheckAccess(
        resourceType = ResourceType.SCHEDULE,
        permission = PermissionType.WRITE,
        message = "일정 생성은 챌린저만 가능합니다."
    )
    public void createSchedule(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateScheduleRequest request
    ) {
        throw new NotImplementedException();
    }

    @Override
    @PostMapping("/with-attendance")
    public Long createScheduleWithAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateScheduleWithAttendanceRequest request
    ) {
        throw new NotImplementedException();
    }

    @Override
    @PostMapping("/study-group")
    public Long createStudyGroupSchedule(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateStudyGroupScheduleRequest request
    ) {
        throw new NotImplementedException();
    }

    @Override
    @PatchMapping("/{scheduleId}")
    @CheckAccess(
        resourceType = ResourceType.SCHEDULE,
        resourceId = "#scheduleId",
        permission = PermissionType.EDIT,
        message = "일정 수정 및 삭제는 작성자나 관리자만 가능합니다."
    )
    public void updateSchedule(
        @PathVariable Long scheduleId,
        @Valid @RequestBody UpdateScheduleRequest request
    ) {
        throw new NotImplementedException();
    }

    @Override
    @DeleteMapping("/{scheduleId}/with-attendance")
    @CheckAccess(
        resourceType = ResourceType.SCHEDULE,
        resourceId = "#scheduleId",
        permission = PermissionType.DELETE,
        message = "일정 수정 및 삭제는 작성자나 관리자만 가능합니다."
    )
    public void deleteScheduleWithAttendance(@PathVariable Long scheduleId) {
        throw new NotImplementedException();
    }

    @Override
    @PatchMapping("/{scheduleId}/location")
    @CheckAccess(
        resourceType = ResourceType.SCHEDULE,
        resourceId = "#scheduleId",
        permission = PermissionType.EDIT,
        message = "일정 수정 및 삭제는 작성자나 관리자만 가능합니다."
    )
    public UpdateScheduleLocationResponse updateScheduleLocation(
        @PathVariable Long scheduleId,
        @Valid @RequestBody UpdateScheduleLocationRequest request
    ) {
        throw new NotImplementedException();
    }
}
