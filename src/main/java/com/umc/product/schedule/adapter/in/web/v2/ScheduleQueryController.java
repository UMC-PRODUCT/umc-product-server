package com.umc.product.schedule.adapter.in.web.v2;

import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.v2.dto.response.ScheduleInfoResponse;
import com.umc.product.schedule.application.port.v2.in.query.GetScheduleUseCase;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleInfo;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
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

    private final GetScheduleUseCase getScheduleUseCase;

    // ========================= 일정 관련 =========================

    @Operation(summary = "내 일정 조회", description = """
        로그인한 사용자가 참여하는 일정 중 Query Param의 `from`, `to` 사이에 시작일이 있는 일정을 모두 조회합니다.

        활동-출석 체크 UI에서 활용하기 위해서는 `isAttendanceRequired` 필드를 `true`로 해서 출석을 트래킹하는 API에 대해서만 조회하면 됩니다.

        운영진이 출석 승인 대기가 필요한 일정을 조회하는 부분은 다른 API를 이용해주세요.
        """
    )
    @GetMapping("/me")
    public List<ScheduleInfoResponse> mySchedule(
        @RequestParam Instant from,
        @RequestParam Instant to,
        @RequestParam Boolean isAttendanceRequired,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        List<ScheduleInfo> results = getScheduleUseCase.getMySchedule(from, to, isAttendanceRequired,
            memberPrincipal.getMemberId());

        return results.stream()
            .map(ScheduleInfoResponse::from)
            .toList();
    }

    @Operation(summary = "일정 상세 조회", description = """
        단일 일정에 대한 정보를 상세하게 조회합니다.
        일정의 기본 정보 및 참여자에 대한 정보를 포함해서 전송합니다.

        참여자의 출석 현황 등에 대해서는 별도의 출석 현황 확인 API를 이용해 주세요.
        """
    )
    @GetMapping("/{scheduleId}")
    public ScheduleInfoResponse details(
        @PathVariable Long scheduleId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return ScheduleInfoResponse.from(
            getScheduleUseCase.getScheduleDetails(scheduleId, memberPrincipal.getMemberId())
        );
    }

    // ========================= 출석 관련 =========================

    // 운영진용: 각 일정에 대한 출석 현황을 조회하는 API, from-to로 기간을 조회할 수 있어야 합니다.
    // 조회 기간과 무관하게 과거 일정 중에서 출석을 승인하지 않은 일정은 계속 표시됩니다.
    // 사유는 제공된 경우에만 표시됩니다.
    @Operation(summary = "[운영진용] 단일 일정 출석 현황 조회", description = """
        Query Param을 이용해서 상세한 필터링을 제공하며, 그 기준은 아래와 같습니다.

        #### 기간 필터링 (시작 시간 기준)
        `from` ~ `to` 사이에 시작 시간이 있는 일정에 대해서 출석 현황을 제공합니다.
        기간을 별도로 지정하지 않은 경우, 기본값은 "요청 시점으로부터 1개월 전" ~ "요청 시점으로부터 24시간 후" 입니다.

        #### 출석 상태 필터링
        `attendanceStatus`를 통해서 요청 상태를 필터링할 수 있습니다.
        제공되지 않은 경우, 모든 상태에 대해서 반환합니다.
        """
    )
    @GetMapping("/{scheduleId}/attendance")
    public ScheduleInfoResponse getAttendanceInfo(
        @RequestParam Instant from,
        @RequestParam Instant to,
        @RequestParam AttendanceStatus attendanceStatus,
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long scheduleId
    ) {
        throw new NotImplementedException();
    }

}
