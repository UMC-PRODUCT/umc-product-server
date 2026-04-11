package com.umc.product.schedule.adapter.in.web.v2;

import com.umc.product.schedule.adapter.in.web.v2.dto.response.ScheduleInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/schedules")
@RequiredArgsConstructor
public class ScheduleQueryController {

    @Operation(summary = "내 일정 조회", description = """
        로그인한 사용자가 참여하는 일정 중 Query Param의 `from`, `to` 사이에 시작일이 있는 일정을 모두 조회합니다.

        운영진이 출석 승인 대기가 필요한 일정을 조회하는 부분은 다른 API를 이용해주세요.
        """
    )
    @GetMapping("/me")
    public List<ScheduleInfoResponse> mySchedule() {
        return null;
    }

    @Operation(summary = "일정 상세 조회", description = """
        단일 일정에 대한 정보를 상세하게 조회합니다.
        일정의 기본 정보 및 참여자에 대한 정보를 포함해서 전송합니다.

        참여자의 출석 현황 등에 대해서는 별도의 출석 현황 확인 API를 이용해 주세요.
        """
    )
    @GetMapping("/{scheduleId}")
    public ScheduleInfoResponse details(@PathVariable String scheduleId) {
        return null;
    }

}
