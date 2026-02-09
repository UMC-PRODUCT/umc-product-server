package com.umc.product.schedule.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.dto.request.CreateScheduleRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.CreateScheduleWithAttendanceRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.CreateStudyGroupScheduleRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.UpdateScheduleLocationRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.UpdateScheduleRequest;
import com.umc.product.schedule.adapter.in.web.dto.response.UpdateScheduleLocationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = SwaggerTag.Constants.SCHEDULE)
public interface ScheduleControllerApi {
    @Operation(summary = "[삭제 예정, 연동 XX]일정 단독 생성", description = "새로운 일정을 생성합니다")
    void createSchedule(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateScheduleRequest request
    );

    @Operation(summary = "일정 + 출석부 통합 생성", description = "일정과 출석부를 함께 생성합니다")
    Long createScheduleWithAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateScheduleWithAttendanceRequest request
    );

    @Operation(summary = "일정 수정", description = "일정 정보를 부분 수정합니다 (변경할 필드만 전송)")
    void updateSchedule(
        @Parameter(description = "일정 ID") @PathVariable Long scheduleId,
        @Valid @RequestBody UpdateScheduleRequest request
    );

    @Operation(summary = "일정 + 출석부 통합 삭제", description = "일정과 연결된 출석부를 함께 삭제합니다")
    void deleteScheduleWithAttendance(
        @Parameter(description = "일정 ID") @PathVariable Long scheduleId
    );

    @Operation(summary = "스터디 그룹 일정 생성", description = "스터디 그룹 일정을 생성하고 그룹 멤버 전원을 출석 대상으로 등록합니다")
    Long createStudyGroupSchedule(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateStudyGroupScheduleRequest request
    );

    @Operation(summary = "일정 출석체크 위치 변경", description = "일정의 출석체크 위치를 변경합니다.")
    UpdateScheduleLocationResponse updateScheduleLocation(
        @Parameter(description = "일정 ID") @PathVariable Long scheduleId,
        @Valid @RequestBody UpdateScheduleLocationRequest request
    );
}
