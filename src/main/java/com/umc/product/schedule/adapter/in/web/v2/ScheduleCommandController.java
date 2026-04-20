package com.umc.product.schedule.adapter.in.web.v2;

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
public class ScheduleCommandController {

    private final CreateScheduleUseCase createScheduleUseCase;
    private final UpdateScheduleUseCase updateScheduleUseCase;

    private final CreateScheduleParticipantUseCase createScheduleParticipantAttendanceUseCase;
    private final UpdateScheduleParticipantUseCase updateScheduleParticipantUseCase;

    @Operation(summary = "일정 생성", description = """
        일정을 생성합니다. `location` 필드를 작성하지 않으실 경우 비대면 일정으로 간주됩니다.

        스터디 일정을 생성하고자 하는 경우에는, 반드시 별도의 API를 이용해서 생성해야 합니다.
        그렇지 않은 경우, 스터디 미진행으로 인한 벌점이 부과될 수 있습니다.

        하루종일 일정의 경우, 클라이언트 단에서 KST 기준으로 시작일 00:00 ~ 종료일 00:00 을 UTC-Based ISO8601 형식으로 보내주셔야 합니다.
        조회할 때도 반대로 KST 기준 00:00에 시작해서 23:59에 끝나는 일정의 경우에 자동으로 하루 종일로 표시해주시면 됩니다.

        일정 생성 시, 초대 가능한 챌린저 수에 제한이 존재합니다.
        일반 챌린저: 50명, 회장단: 100명, 지부장: 300명, 총괄단: 2,000명

        (생성 예정) 일정 생성 시 참여자 목록을 기반으로 해당 인원들이 참여하는 스터디 그룹이 있는지를
        검사하는 API를 토대로 사용자에게 경고 등을 띄울 것을 추천드립니다.

        생성된 일정의 ID 값을 반환합니다.
        """
    )
    @PostMapping
    public Long create(
        @Valid @RequestBody CreateScheduleRequest request,
        @CurrentMember MemberPrincipal memberPrincipal) {

        CreateScheduleCommand command = request.toCommand(memberPrincipal.getMemberId());

        return createScheduleUseCase.create(command);
    }

    @Operation(summary = "일정 수정", description = """
        일정과 관련된 모든 정보를 수정합니다. 제공되지 않은 필드는 변경하지 않는 것으로 간주합니다.
        """
    )
    @PatchMapping("/{scheduleId}")
    public Long edit(
        @PathVariable Long scheduleId,
        @Valid @RequestBody EditScheduleRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        EditScheduleCommand command = request.toCommand(scheduleId, memberPrincipal.getMemberId());

        return updateScheduleUseCase.update(command);
    }

    // 사유 제출까지 여기에 묶어버리는게 맞는 판단일까에 대한 궁금증이 살짝 있음.
    @Operation(summary = "출석 요청하기", description = """
        특정 일정에 대한 출석을 요청합니다. 반환값으로 변경된 출석 상태 및 관련된 정보들을 제공합니다.

        - 이미 출석 요청을 한 경우, 에러가 반환됩니다. (사유 출석 요청 및 이미 출석/지각/결석으로 확정된 경우 등)
        - 일정의 출석 시작 가능 시간이 아직 도래하지 않은 경우 및 일정 종료 시간이 경과된 이후에는 에러가 반환됩니다.
        """
    )
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

    @Operation(summary = "출석 요청이 불가능한 경우, 사유 제출하기", description = """
        위치 인증이 안되거나, 개인 사정이 있어 결석하지만 출석 인정을 요구하는 경우 사유를 제출하기 위하여 사용합니다.

        위치 정보는 클라이언트 단에서 잡히는 경우에 한하여 제공하면 됩니다. 단, 사유는 반드시 제춣하여야 합니다.
        """
    )
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

    // 각 일정에 대한 출석 요청을 승인 또는 기각하는 API, Request는 list 형태로 받을 수 있어야 합니다.
    @Operation(summary = "[운영진용] 출석 요청 승인/거절", description = """
        일정에 대한 출석 요청을 승인 또는 거절합니다.

        결정 권한은 아래와 같습니다. (기준은, 일정이 포함된 기수 기준입니다)
        - 중앙운영사무국 총괄단 이상의 권한을 가지고 있거나, 일정에 참여하는 중앙운영사무국원,

        여러 개의 요청을 한 번에 처리할 수 있도록, DecideAttendanceRequest를 배열로 받습니다.
        모든 요청이 성공적으로 처리된 경우에만 성공으로 반환합니다. (Transaction)
        """
    )
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
