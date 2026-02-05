package com.umc.product.schedule.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.dto.response.MyScheduleResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleDetailResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = Constants.SCHEDULE)
public interface ScheduleQueryControllerApi {

    @Operation(summary = "일정 목록 조회", description = "출석 통계와 함께 일정 목록을 조회합니다")
    List<ScheduleListResponse> getScheduleList();

    @Operation(summary = "월별 내 일정 캘린더/리스트 조회",
        description = "본인이 참여하는 일정을 월 단위로 조회합니다. 조회할 기간의 시작(from)과 끝(to)을 UTC 타임스탬프로 보냅니다.")
    List<MyScheduleResponse> getMyScheduleList(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "조회 시작 시각 (UTC)", example = "2026-03-01T00:00:00Z")
        @RequestParam Instant from,

        @Parameter(description = "조회 종료 시각 (UTC)", example = "2026-04-01T00:00:00Z")
        @RequestParam Instant to
    );

    @Operation(summary = "일정 상세 조회",
        description = "특정 일정의 상세 정보를 조회합니다. D-Day, 위치 좌표 등을 포함합니다.")
    ScheduleDetailResponse getScheduleDetail(
        @Parameter(description = "일정 ID") @PathVariable Long scheduleId
    );
}
