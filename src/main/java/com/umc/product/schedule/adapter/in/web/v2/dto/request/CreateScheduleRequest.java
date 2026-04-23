package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * 일정 생성을 위한 Request DTO 입니다.
 *
 * @param name                 일정명
 * @param description          일정에 대한 설명
 * @param tags                 일정에 관련된 태그
 * @param startsAt             일정 시작 시간
 * @param endsAt               일정 종료 시간
 * @param location             일정 위치 (비대면인 경우, null)
 * @param attendancePolicy     출석/지각/결석과 관련된 정책입니다. 제공되지 않은 경우 출석을 요하지 않는 일정으로 간주합니다.
 * @param participantMemberIds 일정에 참여하는 사용자의 memberId의 배열입니다. 요청한 사용자는 자동으로 참여하도록 설정되며, 중복값은 자동으로 필터링 됩니다.
 */
public record CreateScheduleRequest(
    @Schema(description = "일정 제목", example = "10기 OT", maxLength = 100)
    @NotBlank(message = "일정 제목은 필수입니다")
    @Size(max = 100, message = "일정 제목은 최대 100자까지 입력 가능합니다")
    String name,

    @Schema(description = "메모/설명")
    String description,

    @Schema(description = "태그 목록", example = "[\"STUDY\", \"PROJECT\"]")
    @NotNull(message = "태그는 필수입니다")
    @Size(min = 1, message = "최소 1개 이상의 태그를 선택해야 합니다")
    Set<ScheduleTag> tags,

    @Schema(description = "시작 일시 (UTC ISO8601. 예: 2026-05-21T01:00:00Z)", example = "2026-05-21T01:00:00Z")
    @NotNull(message = "시작 일시는 필수입니다")
    Instant startsAt,

    @Schema(description = "종료 일시 (UTC ISO8601. 예: 2026-08-20T03:00:00Z)", example = "2026-08-20T03:00:00Z")
    @NotNull(message = "종료 일시는 필수입니다")
    Instant endsAt,
    // 하루종일 일정은 따로 서버측에서 저장하지 않고,
    // 클라이언트 단에서 KST 기준 Instant로 알아서 변환하도록 합니다.

    ScheduleLocationRequest location,

    ScheduleAttendancePolicyRequest attendancePolicy,

    // 참여자는 중복되지 않도록 Set으로 받습니다.
    // 참여자 목록에는 반드시 요청한 사용자가 포함되어 있어야 하며,
    // 서버 측 생성자에서 add()를 통해서 강제로 참여시켜야 합니다.
    // (기획단 변경이 있기 전까지는 해당 사항을 유지합니다.)
    @Schema(description = "참여자 Member ID 목록", example = "[1, 2, 3]")
    Set<Long> participantMemberIds
) {
    public CreateScheduleCommand toCommand(Long authorMemberId) {

        Set<Long> participants = participantMemberIds != null
            ? new HashSet<>(participantMemberIds)
            : new HashSet<>();
        participants.add(authorMemberId);  // 일정 생성자 추가

        return CreateScheduleCommand.builder()
            .name(name)
            .description(description)
            .tags(tags)
            .authorMemberId(authorMemberId)
            .startsAt(startsAt)
            .endsAt(endsAt)
            .location(location)
            .attendancePolicy(attendancePolicy)
            .participantMemberIds(participants)
            .build();
    }
}
