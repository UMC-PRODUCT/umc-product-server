package com.umc.product.schedule.application.service.query;

import com.umc.product.schedule.application.port.in.query.GetScheduleDetailUseCase;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleDetailInfo;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.v2.in.query.GetScheduleUseCase;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleInfo.ScheduleAttendancePolicyInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleInfo.ScheduleLocationInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleInfo.ScheduleParticipantInfo;
import com.umc.product.schedule.application.port.v2.out.LoadScheduleParticipantPort;
import com.umc.product.schedule.application.port.v2.out.dto.ScheduleParticipantDetailDto;
import com.umc.product.schedule.domain.AttendancePolicy;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleQueryService implements
    GetScheduleUseCase,
    GetScheduleDetailUseCase {

    private final LoadSchedulePort loadSchedulePort;
    private final LoadScheduleParticipantPort loadScheduleParticipantPort;

    // 내 일정 조회
    @Override
    public List<ScheduleInfo> getMySchedule(
        Instant from,
        Instant to,
        Boolean isAttendanceRequired,
        Long memberId
    ) {
        // 사용자가 참여하는 일정 조회
        List<Schedule> schedules = loadSchedulePort.findMySchedules(memberId, from, to, isAttendanceRequired);

        if (schedules.isEmpty()) {
            return List.of();
        }

        // 조회된 일정들의 Id 추출
        List<Long> scheduleIds = schedules.stream()
            .map(Schedule::getId)
            .toList();

        // 참여자 상세 정보 일괄 조회 후 scheduleId 기준으로 그룹화
        List<ScheduleParticipantDetailDto> allParticipants =
            loadScheduleParticipantPort.findParticipantDetailsByScheduleIds(scheduleIds);

        Map<Long, List<ScheduleParticipantDetailDto>> participantsMap = allParticipants.stream()
            .collect(Collectors.groupingBy(ScheduleParticipantDetailDto::scheduleId));

        // 도메인 -> List<ScheduleInfo> 매핑
        return schedules.stream().map(schedule -> {
            // 순회 중인 scheduleId를 key로 하여 참여자 목록 꺼냄, 참여자가 없으면 빈 리스트 반환
            List<ScheduleParticipantDetailDto> participants = participantsMap.getOrDefault(schedule.getId(), List.of());

            // 위에서 찾은 참여자 목록(participants)을 순회하면서 그 중 내 Id(memberId)랑 일치하는 걸 꺼냄
            // 찾았으면 출석 상태(attendanceStatus)를 가져오고, 못 찾으면 null
            AttendanceStatus myStatus = participants.stream()
                .filter(p -> p.memberId().equals(memberId))
                .findFirst()
                .map(ScheduleParticipantDetailDto::attendanceStatus)
                .orElse(null);

            // ScheduleInfo 객체 생성
            return new ScheduleInfo(
                schedule.getId(),
                schedule.getName(),
                schedule.getDescription(),
                schedule.getTags(),
                schedule.getAuthorMemberId(),
                schedule.getStartsAt(),
                schedule.getEndsAt(),

                schedule.getLocation() == null, // isOnline
                mapLocation(schedule),

                myStatus,
                schedule.getPolicy() != null, // isAttendanceChecked
                mapPolicy(schedule),

                true, // 이 API 자체가 '내가 참여하는' 일정이므로 항상 true
                mapParticipants(participants)
            );
        }).toList();
    }

    // ScheduleLocationInfo 변환 헬퍼 메서드
    private ScheduleLocationInfo mapLocation(Schedule schedule) {
        if (schedule.getLocation() == null) {
            return null;
        }
        return new ScheduleLocationInfo(
            schedule.getLocation().getY(), // latitude (위도)
            schedule.getLocation().getX(), // longitude (경도)
            schedule.getLocationName()
        );
    }

    // ScheduleAttendancePolicyInfo 변환 헬퍼 메서드
    private ScheduleAttendancePolicyInfo mapPolicy(Schedule schedule) {
        AttendancePolicy policy = schedule.getPolicy();
        if (policy == null) {
            return null;
        }

        Instant start = schedule.getStartsAt();

        // 정책을 따른 시간 계산
        Instant checkInStart = start.minus(policy.getEarlyCheckInMinutes(), ChronoUnit.MINUTES);
        Instant onTimeEnd = start.plus(policy.getAttendanceGraceMinutes(), ChronoUnit.MINUTES);
        Instant lateEnd = onTimeEnd.plus(policy.getLateToleranceMinutes(), ChronoUnit.MINUTES);

        return new ScheduleAttendancePolicyInfo(checkInStart, onTimeEnd, lateEnd);
    }

    // ScheduleParticipantInfo 변환 헬퍼 메서드
    private List<ScheduleParticipantInfo> mapParticipants(List<ScheduleParticipantDetailDto> participants) {
        return participants.stream()
            .map(p -> new ScheduleParticipantInfo(
                p.memberId(), p.name(), p.nickname(),
                p.schoolId(), p.schoolName(), p.profileImageUrl()
            )).toList();
    }

    @Override
    public ScheduleDetailInfo getScheduleDetail(Long scheduleId) {
        return null;
    }


}
