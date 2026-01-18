package com.umc.product.schedule.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = Constants.SCHEDULE)
public interface ScheduleQueryControllerApi {

    @Operation(summary = "일정 목록 조회", description = "출석 통계와 함께 일정 목록을 조회합니다")
    List<ScheduleListResponse> getScheduleList();
}
