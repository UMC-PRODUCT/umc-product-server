package com.umc.product.schedule.adapter.in.web.v2;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.v2.dto.response.AdminScheduleInfoResponse;
import com.umc.product.schedule.adapter.in.web.v2.dto.response.ScheduleCapabilitiesResponse;
import com.umc.product.schedule.adapter.in.web.v2.dto.response.ScheduleInfoResponse;
import com.umc.product.schedule.application.port.in.query.GetScheduleCapabilitiesUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleUseCase;
import com.umc.product.schedule.application.port.in.query.dto.AdminScheduleInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleInfo;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
public class ScheduleQueryV2Controller {

    private final GetScheduleUseCase getScheduleUseCase;
    private final GetScheduleCapabilitiesUseCase getScheduleCapabilitiesUseCase;

    // ========================= 일정 관련 =========================

    @Operation(summary = "[프론트엔드용] 일정 생성, 수정 관련 권한 조회", description = """
        현재 사용자의 일정 생성, 수정 관련 권한을 조회합니다. 일정 생성, 수정 화면에서 사용하시면 됩니다.

        - `canCreateSchedule` : 일정 생성 가능 여부
        - `canCreateAttendanceRequiredSchedule` : 출석 정책 포함 일정 생성 가능 여부 (운영진 : true, 일반 챌린저 : false)
        - `maxParticipantCount` : 직책별 최대 초대 가능 인원
        """
    )
    @GetMapping("/capabilities")
    public ScheduleCapabilitiesResponse getCapabilities(
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return ScheduleCapabilitiesResponse.from(
            getScheduleCapabilitiesUseCase.getCapabilities(memberPrincipal.getMemberId())
        );
    }

    @CheckAccess(
        resourceType = ResourceType.SCHEDULE,
        permission = PermissionType.READ,
        message = "내 일정 조회는 '챌린저 활동 기록이 있는 사용자'만 가능합니다."
    )
    @Operation(summary = "내 일정 조회", description = """
        로그인한 사용자가 참여하는 일정 중 Query Param의 `from`, `to` 사이에 시작일이 있는 일정을 모두 조회합니다.

        활동-출석 체크 UI에서 활용하기 위해서는 `isAttendanceRequired` 필드를 `true`로 해서 출석을 트래킹하는 API에 대해서만 조회하면 됩니다.

        운영진이 출석 승인 대기가 필요한 일정을 조회하는 부분은 다른 API를 이용해주세요.
        """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "403", description = """
            AUTHORIZATION-0001 : 권한이 없습니다.
            """,
            content = @Content
        )
    })
    @GetMapping("/me")
    public List<ScheduleInfoResponse> mySchedules(
        @RequestParam Instant from,
        @RequestParam Instant to,
        @RequestParam Boolean isAttendanceRequired,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        List<ScheduleInfo> results = getScheduleUseCase.searchMySchedules(
            from, to, isAttendanceRequired,
            memberPrincipal.getMemberId());

        return results.stream()
            .map(ScheduleInfoResponse::from)
            .toList();
    }

    @CheckAccess(
        resourceType = ResourceType.SCHEDULE,
        permission = PermissionType.READ,
        message = "일정 상세 조회는 '챌린저 활동 기록이 있는 사용자'만 가능합니다."
    )
    @Operation(summary = "일정 상세 조회", description = """
        단일 일정에 대한 정보를 상세하게 조회합니다.
        일정의 기본 정보 및 참여자에 대한 정보를 포함해서 전송합니다.

        참여자의 출석 현황 등에 대해서는 별도의 출석 현황 확인 API를 이용해 주세요.
        """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "403", description = """
            AUTHORIZATION-0002 : 해당 리소스에 접근할 권한이 없습니다.
            """,
            content = @Content
        ),
        @ApiResponse(responseCode = "404", description = """
            SCHEDULE-0009 : 일정을 찾을 수 없습니다.
            """,
            content = @Content
        )
    })
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

    // 운영진용: 기간에 기반하여 일정에 대한 출석 현황을 조회하는 API, from-to로 기간을 조회할 수 있어야 합니다.
    // 조회 기간과 무관하게 과거 일정 중에서 출석을 승인하지 않은 일정은 계속 표시됩니다.
    // 사유는 제공된 경우에만 표시됩니다.
    @CheckAccess(
        resourceType = ResourceType.ATTENDANCE,
        permission = PermissionType.READ,
        message = "일정 목록 출석 현황 조회는 '운영진 활동 이력이 있는 사용자'만 가능합니다."
    )
    @Operation(summary = "[운영진용] 일정들의 출석 현황 조회", description = """
        Query Param을 이용해서 상세한 필터링을 제공하며, 그 기준은 아래와 같습니다.

        #### 기간 필터링 (시작 시간 기준)
        `from` ~ `to` 사이에 시작 시간이 있는 일정에 대해서 출석 현황을 제공합니다.
        기간을 별도로 지정하지 않은 경우, 기본값은 "요청 시점으로부터 1개월 전" ~ "요청 시점으로부터 24시간 후" 입니다.
        조회 기간과 무관하게 과거 일정 중에서 출석을 승인하지 않은 일정은 계속 표시됩니다.

        #### 출석 상태 필터링
        `attendanceStatus`를 통해서 요청 상태를 필터링할 수 있습니다.
        제공되지 않은 경우, 모든 상태에 대해서 반환합니다.

        #### 사용자 직책역할별 조회 범위
        - 중앙 총괄단/운영진 : 본인 참석 일정
        - 학교 회장단 : 본인 생성 일정 + 교내 인원이 포함된 스터디 그룹 일정 + 교내 파트장이 멘토인 스터디 그룹 일정
        - 교내 파트장 : 본인 생성 일정 + 본인 멘토 스터디 그룹 일정
        - 기타 운영진 : 본인 생성 일정
        """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = """
            SCHEDULE-0026 : 일정의 참여자가 아닙니다.
            """,
            content = @Content
        ),
        @ApiResponse(responseCode = "403", description = """
            AUTHORIZATION-0001 : 권한이 없습니다.
            """,
            content = @Content
        ),
        @ApiResponse(responseCode = "404", description = """
            SCHEDULE-0009 : 일정을 찾을 수 없습니다.
            """,
            content = @Content
        )
    })
    @GetMapping("/attendance")
    public List<AdminScheduleInfoResponse> getAttendanceInfoList(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) AttendanceStatus attendanceStatus,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Instant searchFrom = (from != null) ? from : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant searchTo = (to != null) ? to : Instant.now().plus(24, ChronoUnit.HOURS);

        List<AdminScheduleInfo> results = getScheduleUseCase.searchAdminSchedules(
            searchFrom, searchTo, attendanceStatus, memberPrincipal.getMemberId()
        );

        return results.stream()
            .map(AdminScheduleInfoResponse::from)
            .toList();
    }

    // 운영진용: 단일 일정에 대한 출석 현황을 조회하는 API
    // 사유는 제공된 경우에만 표시됩니다.
    @CheckAccess(
        resourceType = ResourceType.ATTENDANCE,
        permission = PermissionType.READ,
        message = "단일 일정 출석 현황 조회는 '해당 일정이 진행되는 기수의 운영진'만 가능합니다."
    )
    @Operation(summary = "[운영진용] 단일 일정 출석 현황 조회", description = """
        Query Param을 이용해서 상세한 필터링을 제공하며, 그 기준은 아래와 같습니다.

        #### 출석 상태 필터링
        `attendanceStatus`를 통해서 요청 상태를 필터링할 수 있습니다.
        제공되지 않은 경우, 모든 상태에 대해서 반환합니다.
        """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = """
            SCHEDULE-0021 : 출석 정책이 존재하지 않아 출석 요청이 불가능한 일정입니다.<br>
            SCHEDULE-0026 : 일정의 참여자가 아닙니다.
            """,
            content = @Content
        ),
        @ApiResponse(responseCode = "403", description = """
            AUTHORIZATION-0002 : 해당 리소스에 접근할 권한이 없습니다.
            """,
            content = @Content
        ),
        @ApiResponse(responseCode = "404", description = """
            SCHEDULE-0009 : 일정을 찾을 수 없습니다.
            """,
            content = @Content
        )
    })
    @GetMapping("/{scheduleId}/attendance")
    public AdminScheduleInfoResponse getAttendanceInfo(
        @RequestParam(required = false) AttendanceStatus attendanceStatus,
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long scheduleId
    ) {
        return AdminScheduleInfoResponse.from(
            getScheduleUseCase.getAdminSchedule(
                scheduleId,
                memberPrincipal.getMemberId(),
                attendanceStatus
            )
        );
    }
}
