package com.umc.product.schedule.adapter.in.web.v2;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.v2.dto.request.CreateScheduleRequest;
import com.umc.product.schedule.adapter.in.web.v2.dto.request.DecideAttendanceRequest;
import com.umc.product.schedule.adapter.in.web.v2.dto.request.EditScheduleRequest;
import com.umc.product.schedule.adapter.in.web.v2.dto.request.ExcuseScheduleAttendanceRequest;
import com.umc.product.schedule.adapter.in.web.v2.dto.request.ScheduleAttendanceRequest;
import com.umc.product.schedule.adapter.in.web.v2.dto.response.ScheduleParticipantAttendanceInfoResponse;
import com.umc.product.schedule.application.port.in.command.CreateScheduleParticipantUseCase;
import com.umc.product.schedule.application.port.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.UpdateScheduleParticipantUseCase;
import com.umc.product.schedule.application.port.in.command.UpdateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.DecideAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.EditScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.ExcuseScheduleAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.ScheduleAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.result.ScheduleParticipantAttendanceResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule V2 | Command", description = "일정을 생성하거나 수정하고, 출석 관련 요청을 처리합니다.")
public class ScheduleCommandV2Controller {

    private final CreateScheduleUseCase createScheduleUseCase;
    private final UpdateScheduleUseCase updateScheduleUseCase;

    private final CreateScheduleParticipantUseCase createScheduleParticipantAttendanceUseCase;
    private final UpdateScheduleParticipantUseCase updateScheduleParticipantUseCase;

    @CheckAccess(
        resourceType = ResourceType.SCHEDULE,
        permission = PermissionType.WRITE,
        message = "일정 생성은 '챌린저 활동 기록이 있는 사용자'만 가능합니다."
    )
    @Operation(summary = "일정 생성", description = """
        일정을 생성합니다. `location` 필드를 작성하지 않으실 경우 비대면 일정으로 간주됩니다.

        스터디 일정을 생성하고자 하는 경우에는, 반드시 별도의 API를 이용해서 생성해야 합니다.
        그렇지 않은 경우, 스터디 미진행으로 인한 벌점이 부과될 수 있습니다.

        하루종일 일정의 경우, 클라이언트 단에서 KST 기준으로 시작일 00:00 ~ 종료일 00:00 을 UTC-Based ISO8601 형식으로 보내주셔야 합니다.
        조회할 때도 반대로 KST 기준 00:00에 시작해서 23:59에 끝나는 일정의 경우에 자동으로 하루 종일로 표시해주시면 됩니다.

        출석 요청 관련 일시 필드는 운영진만 입력 가능합니다.
        또한 일정 생성 시, 초대 가능한 챌린저 수에 제한이 존재합니다.
        - 일반 챌린저: 50명, 회장단: 100명, 파트장/기타 운영진: 100명, 지부장: 300명, 중앙 운영진: 300명, 총괄단: 2,000명

        이 부분에 대한 사용자별 권한 확인은 '[프론트엔드용] 일정 생성 관련 권한 조회' API를 사용해주세요.

        (생성 예정) 일정 생성 시 참여자 목록을 기반으로 해당 인원들이 참여하는 스터디 그룹이 있는지를
        검사하는 API를 토대로 사용자에게 경고 등을 띄울 것을 추천드립니다.

        생성된 일정의 ID 값을 반환합니다.
        """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = """
            SCHEDULE-0006 : 시작 시간은 종료 시간보다 이전이어야 합니다.<br>
            SCHEDULE-0010 : 태그는 최소 1개 이상 선택해야 합니다.<br>
            SCHEDULE-0025 : 현재 기수의 일정만 생성할 수 있습니다.<br>
            SCHEDULE-0030 : 초대 가능한 최대 참여자 수를 초과했습니다.<br>
            SCHEDULE-0032 : 초대하려는 참여자에 유효하지 않은 사용자가 포함되어 있습니다.<br>
            CHALLENGER-0009 : 활성 또는 수료 상태의 사용자만 일정 생성이 가능합니다.
            """,
            content = @Content
        ),
        @ApiResponse(responseCode = "403", description = """
            AUTHORIZATION-0001 : 권한이 없습니다.<br>
            SCHEDULE-0029 : 일정을 생성할 수 없습니다. 챌린저 활동 이력이 필요합니다.<br>
            SCHEDULE-0031 : 출석을 요하는 일정을 생성할 권한이 없습니다.
            """,
            content = @Content
        )
    })
    @PostMapping
    public Long create(
        @Valid @RequestBody CreateScheduleRequest request,
        @CurrentMember MemberPrincipal memberPrincipal) {

        CreateScheduleCommand command = request.toCommand(memberPrincipal.getMemberId());

        return createScheduleUseCase.create(command);
    }

    @CheckAccess(
        resourceType = ResourceType.SCHEDULE,
        resourceId = "#scheduleId",
        permission = PermissionType.EDIT,
        message = "생성자 본인' 또는 '해당 일정 기수의 최고 운영 관리자'만 가능합니다."
    )
    @Operation(summary = "일정 수정", description = """
        일정과 관련된 모든 정보를 수정합니다. 제공되지 않은 필드는 변경하지 않는 것으로 간주합니다.

        ---
        ### `isOnline` (대면/비대면 전환 플래그)
        일정의 대면 <-> 비대면 상태를 명시적으로 전환할 때 사용합니다.
        * **`null` (또는 생략)** : 기존 상태 유지
        > ⚠️ **주의:** 기존 대면 일정에서 **장소(location)만 다른 곳으로 변경**하는 경우, 속성이 전환되는 것이 아니므로 반드시 `null`로 보내주세요.
        * **`true`** : **비대면** 일정으로 전환 (기존 장소 데이터 삭제)
        * **`false`** : **대면** 일정으로 전환 (반드시 `location` 데이터를 함께 전송해야 함)
        ---
        ### `isAttendanceRequired` (출석 체크 여부 전환 플래그)
        일정의 출석 체크 상태를 명시적으로 전환할 때 사용합니다.
        * **`null` (또는 생략)** : 기존 출석 상태 유지
        * **`true`** : **출석 O** 일정으로 전환 (반드시 `attendancePolicy` 데이터를 함께 전송해야 함)
        * **`false`** : **출석 X** 일정으로 전환 (기존 출석 정책 데이터 삭제)
        """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = """
            SCHEDULE-0006 : 시작 시간은 종료 시간보다 이전이어야 합니다.<br>
            SCHEDULE-0010 : 태그는 최소 1개 이상 선택해야 합니다.<br>
            SCHEDULE-0020 : 대면 일정은 위치 정보가 필수입니다.<br>
            SCHEDULE-0024 : 비대면 일정으로 변경 시 위치 정보를 포함할 수 없습니다.<br>
            SCHEDULE-0027 : 출석을 요하는 일정의 출석 정책은 필수입니다.<br>
            SCHEDULE-0028 : 시작된 일정은 수정이 불가합니다.<br>
            SCHEDULE-0030 : 초대 가능한 최대 참여자 수를 초과했습니다.<br>
            SCHEDULE-0032 : 초대하려는 참여자에 유효하지 않은 사용자가 포함되어 있습니다.
            """,
            content = @Content
        ),
        @ApiResponse(responseCode = "403", description = """
            AUTHORIZATION-0002 : 해당 리소스에 접근할 권한이 없습니다.<br>
            SCHEDULE-0031 : 출석을 요하는 일정을 생성할 권한이 없습니다.
            """,
            content = @Content
        ),
        @ApiResponse(responseCode = "404", description = """
            SCHEDULE-0009 : 일정을 찾을 수 없습니다.
            """,
            content = @Content
        )
    })
    @PatchMapping("/{scheduleId}")
    public Long edit(
        @PathVariable Long scheduleId,
        @Valid @RequestBody EditScheduleRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        EditScheduleCommand command = request.toCommand(scheduleId, memberPrincipal.getMemberId());

        return updateScheduleUseCase.update(command);
    }

    // ========================= 출석 관련 =========================

    @CheckAccess(
        resourceType = ResourceType.ATTENDANCE,
        resourceId = "#scheduleId",
        permission = PermissionType.WRITE,
        message = "출석 요청은 '챌린저 활동 기록이 있는 사용자'면서 '일정에 참여하는 사용자'만 가능합니다."
    )
    @Operation(summary = "출석 요청하기", description = """
        특정 일정에 대한 출석을 요청합니다. 반환값으로 변경된 출석 상태 및 관련된 정보들을 제공합니다.

        - 이미 출석 요청을 한 경우, 에러가 반환됩니다. (사유 출석 요청 및 이미 출석/지각/결석으로 확정된 경우 등)
        - 일정의 출석 시작 가능 시간이 아직 도래하지 않은 경우 및 일정 종료 시간이 경과된 이후에는 에러가 반환됩니다.
        """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = """
            SCHEDULE-0011 : 기존 출석 요청이 존재합니다.<br>
            SCHEDULE-0018 : 종료된 일정에 대한 출석 요청은 허용되지 않습니다.<br>
            SCHEDULE-0019 : 출석 가능한 시간 이전입니다. 출석 가능한 시간 이후에 다시 시도해주세요.<br>
            SCHEDULE-0021 : 출석 정책이 존재하지 않아 출석 요청이 불가능한 일정입니다.<br>
            SCHEDULE-0022 : 일정에 대한 참석자 정보가 존재하지 않습니다.<br>
            SCHEDULE-0023 : 사용자의 출석 인증 범위 내의 존재 여부가 확인되지 않습니다.
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
    @PostMapping("/{scheduleId}/attendances/request")
    public ScheduleParticipantAttendanceInfoResponse requestAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long scheduleId,
        @Valid @RequestBody ScheduleAttendanceRequest request
    ) {
        ScheduleAttendanceCommand command = request.toCommand(scheduleId, memberPrincipal.getMemberId());

        return ScheduleParticipantAttendanceInfoResponse.from(
            createScheduleParticipantAttendanceUseCase.createScheduleParticipantWithAttendance(command)
        );
    }

    @CheckAccess(
        resourceType = ResourceType.ATTENDANCE,
        resourceId = "#scheduleId",
        permission = PermissionType.WRITE,
        message = "출석 사유 제출은 '챌린저 활동 기록이 있는 사용자'면서 '일정에 참여하는 사용자'만 가능합니다."
    )
    @Operation(summary = "출석 요청이 불가능한 경우, 사유 제출하기", description = """
        위치 인증이 안되거나, 개인 사정이 있어 결석하지만 출석 인정을 요구하는 경우 사유를 제출하기 위하여 사용합니다.

        위치 정보는 클라이언트 단에서 잡히는 경우에 한하여 제공하면 됩니다. 단, 사유는 반드시 제출하여야 합니다.
        """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = """
            SCHEDULE-0013 : 출석 사유 제출은 첫 요청, 결석 또는 지각 상태에서만 가능합니다.<br>
            SCHEDULE-0016 : 출석 인정을 요청하는 사유가 제공되지 않았거나 비어있습니다.<br>
            SCHEDULE-0021 : 출석 정책이 존재하지 않아 출석 요청이 불가능한 일정입니다.<br>
            SCHEDULE-0022 : 일정에 대한 참석자 정보가 존재하지 않습니다.
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
    @PostMapping("/{scheduleId}/attendances/excuse")
    public ScheduleParticipantAttendanceInfoResponse excuseAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long scheduleId,
        @Valid @RequestBody ExcuseScheduleAttendanceRequest request
    ) {
        ExcuseScheduleAttendanceCommand command = request.toCommand(scheduleId, memberPrincipal.getMemberId());

        return ScheduleParticipantAttendanceInfoResponse.from(
            createScheduleParticipantAttendanceUseCase.createExcusedScheduleParticipantWithAttendance(command)
        );
    }

    @CheckAccess(
        resourceType = ResourceType.ATTENDANCE,
        resourceId = "#scheduleId",
        permission = PermissionType.APPROVE,
        message = "출석 요청 승인/거절은 '해당 일정이 진행되는 기수의 운영진'만 가능합니다."
    )
    // 각 일정에 대한 출석 요청을 승인 또는 기각하는 API, Request는 list 형태로 받을 수 있어야 합니다.
    @Operation(summary = "[운영진용] 출석 요청 승인/거절", description = """
        일정에 대한 출석 요청을 승인 또는 거절합니다.

        결정 권한은 아래와 같습니다. (기준은, 일정이 포함된 기수 기준입니다)
        - 중앙운영사무국 총괄단 이상의 권한을 가지고 있거나, 일정에 참여하는 중앙운영사무국원,

        여러 개의 요청을 한 번에 처리할 수 있도록, DecideAttendanceRequest를 배열로 받습니다.
        모든 요청이 성공적으로 처리된 경우에만 성공으로 반환합니다. (Transaction)
        """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = """
            SCHEDULE-0012 : 출석 요청이 존재하지 않습니다. 출석 요청을 생성하고 다시 시도해주세요.<br>
            SCHEDULE-0014 : 현재 출석 상태에서는 승인이 불가능합니다.<br>
            SCHEDULE-0015 : 출석 요청에 대한 거절을 할 수 없는 상태입니다.<br>
            SCHEDULE-0017 : 해당 출석 요청은 운영진의 승인 또는 기각을 필요로 하는 상태가 아닙니다.<br>
            SCHEDULE-0021 : 출석 정책이 존재하지 않아 출석 요청이 불가능한 일정입니다.<br>
            SCHEDULE-0022 : 일정에 대한 참석자 정보가 존재하지 않습니다.
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
    @PostMapping("/{scheduleId}/attendances/decide")
    public List<ScheduleParticipantAttendanceInfoResponse> decideAttendances(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long scheduleId,
        @Valid @RequestBody List<DecideAttendanceRequest> requests
    ) {
        // List<Request> -> List<Command> 변환
        List<DecideAttendanceCommand> commands = requests.stream()
            .map(request -> request.toCommand(scheduleId, memberPrincipal.getMemberId()))
            .toList();

        List<ScheduleParticipantAttendanceResult> results = updateScheduleParticipantUseCase.decideAttendances(
            commands);

        return results.stream()
            .map(ScheduleParticipantAttendanceInfoResponse::from)
            .toList();
    }

    // TODO: 일정 신고 API (본인이 참여하지 않는 일정에 강제로 초대당한 경우)

}
