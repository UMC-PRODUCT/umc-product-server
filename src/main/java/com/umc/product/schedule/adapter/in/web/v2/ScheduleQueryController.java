package com.umc.product.schedule.adapter.in.web.v2;

import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.v2.dto.response.ScheduleInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule V2 | Query", description = "일정 및 출석 관련 내용들을 조회합니다.")
public class ScheduleQueryController {

    // ========================= 일정 관련 =========================

    @Operation(summary = "내 일정 조회", description = """
        로그인한 사용자가 참여하는 일정 중 Query Param의 `from`, `to` 사이에 시작일이 있는 일정을 모두 조회합니다.

        운영진이 출석 승인 대기가 필요한 일정을 조회하는 부분은 다른 API를 이용해주세요.
        """
    )
    @GetMapping("/me")
    public List<ScheduleInfoResponse> mySchedule(
        @RequestParam Instant from,
        @RequestParam Instant to,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        throw new NotImplementedException();
    }

    @Operation(summary = "일정 상세 조회", description = """
        단일 일정에 대한 정보를 상세하게 조회합니다.
        일정의 기본 정보 및 참여자에 대한 정보를 포함해서 전송합니다.

        참여자의 출석 현황 등에 대해서는 별도의 출석 현황 확인 API를 이용해 주세요.
        """
    )
    @GetMapping("/{scheduleId}")
    public ScheduleInfoResponse details(@PathVariable String scheduleId) {
        throw new NotImplementedException();
    }

    // ========================= 출석 관련 =========================

    // 출석 가능한 세션을 조회하는 API
    @Operation(summary = "일정 상세 조회", description = """
        단일 일정에 대한 정보를 상세하게 조회합니다.
        일정의 기본 정보 및 참여자에 대한 정보를 포함해서 전송합니다.

        참여자의 출석 현황 등에 대해서는 별도의 출석 현황 확인 API를 이용해 주세요.
        """
    )
    @GetMapping("/me/available")
    public ScheduleInfoResponse availableSchedule(@CurrentMember MemberPrincipal memberPrincipal) {
        throw new NotImplementedException();
    }

    // 운영진용: 각 일정에 대한 출석 현황을 조회하는 API, from-to로 기간을 조회할 수 있어야 합니다.
    // 조회 기간과 무관하게 과거 일정 중에서 출석을 승인하지 않은 일정은 계속 표시됩니다.
    // 사유는 제공된 경우에만 표시됩니다.
    @Operation(summary = "[운영진용] 단일 일정 출석 현황 조회", description = """
        Query Param의 `from` ~ `to` 사이에 시작 시간이 있는 일정에 대해서 출석 현황을 제공합니다.
        출석 요청 및 승인 여부에 관계 없이 모두 표시됩니다.
        """
    )
    @GetMapping("/{scheduleId}/attendance")
    public ScheduleInfoResponse getAttendanceInfo(
        @RequestParam Instant from,
        @RequestParam Instant to,
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable String scheduleId
    ) {
        throw new NotImplementedException();
    }

}
