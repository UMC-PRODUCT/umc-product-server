package com.umc.product.schedule.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.dto.response.MyScheduleCalendarResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleDetailResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = Constants.SCHEDULE)
public interface ScheduleQueryControllerApi {

    @Operation(summary = "일정 목록 조회", description = "출석 통계와 함께 일정 목록을 조회합니다")
    List<ScheduleListResponse> getScheduleList();

    @Operation(summary = "월별 내 일정 캘린더 조회",
            description = "본인이 참여하는 일정을 월 단위로 조회합니다. AttendanceRecord에 등록된 일정만 포함됩니다.")
    List<MyScheduleCalendarResponse> getMyCalendar(
            @CurrentMember MemberPrincipal memberPrincipal,
            @Parameter(description = "연도 (예: 2026)") @RequestParam int year,
            @Parameter(description = "월 (1~12)") @RequestParam int month
    );

    @Operation(summary = "일정 상세 조회",
            description = "특정 일정의 상세 정보를 조회합니다. D-Day, 위치 좌표 등을 포함합니다.")
    ScheduleDetailResponse getScheduleDetail(
            @Parameter(description = "일정 ID") @PathVariable Long scheduleId
    );
}
