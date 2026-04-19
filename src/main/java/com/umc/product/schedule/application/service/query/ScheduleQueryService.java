package com.umc.product.schedule.application.service.query;

import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.v2.in.query.GetScheduleUseCase;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleBaseInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleInfo.ScheduleAttendancePolicyInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleInfo.ScheduleLocationInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleInfo.ScheduleParticipantInfo;
import com.umc.product.schedule.application.port.v2.out.LoadScheduleParticipantPort;
import com.umc.product.schedule.application.port.v2.out.dto.ScheduleParticipantDetailDto;
import com.umc.product.schedule.domain.AttendancePolicy;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
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
    GetScheduleUseCase {
    private final LoadSchedulePort loadSchedulePort;
    private final LoadScheduleParticipantPort loadScheduleParticipantPort;

    // 내 일정 조회
    @Override
    public List<ScheduleInfo> searchMySchedule(
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
        return schedules.stream()
            .map(schedule -> {
                // 순회 중인 scheduleId를 key로 하여 참여자 목록 꺼냄, 참여자가 없으면 빈 리스트 반환
                List<ScheduleParticipantDetailDto> participants =
                    participantsMap.getOrDefault(schedule.getId(), List.of());

                return createScheduleInfo(schedule, participants, memberId);
            }).toList();
    }

    @Override
    public ScheduleInfo getScheduleDetails(Long scheduleId, Long memberId) {

        Schedule schedule = loadSchedulePort.findById(scheduleId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        List<ScheduleParticipantDetailDto> participants = loadScheduleParticipantPort.findParticipantDetailsByScheduleId(
            scheduleId);

        return createScheduleInfo(schedule, participants, memberId);
    }

    // ======= Helper Method =======

    // ScheduleBaseInfo 변환 메서드
    private ScheduleBaseInfo createBaseInfo(Schedule schedule) {
        return new ScheduleBaseInfo(
            schedule.getId(),
            schedule.getName(),
            schedule.getDescription(),
            schedule.getTags(),
            schedule.getAuthorMemberId(),
            schedule.getStartsAt(),
            schedule.getEndsAt(),
            schedule.getLocation() == null, // isOnline
            mapLocation(schedule),
            schedule.getPolicy() != null, // isAttendanceChecked
            mapPolicy(schedule)
        );
    }

    // 일정 도메인 + 참여자 DTO 리스트 -> ScheduleInfo 변환 메서드
    private ScheduleInfo createScheduleInfo(
        Schedule schedule,
        List<ScheduleParticipantDetailDto> participants,
        Long requesterMemberId
    ) {
        ScheduleBaseInfo baseInfo = createBaseInfo(schedule);

        // 요청자의 출석 상태 및 참여자 여부 확인
        AttendanceStatus myStatus = null;
        boolean isParticipant = false;

        for (ScheduleParticipantDetailDto p : participants) {
            if (p.memberId().equals(requesterMemberId)) {
                myStatus = p.attendanceStatus();
                isParticipant = true;
                break;
            }
        }

        // ScheduleInfo 반환
        return new ScheduleInfo(
            baseInfo,
            myStatus,
            isParticipant,
            mapParticipants(participants)
        );
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

}
