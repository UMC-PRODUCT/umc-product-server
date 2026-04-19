package com.umc.product.schedule.application.service.query;

import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.v2.in.query.GetScheduleUseCase;
import com.umc.product.schedule.application.port.v2.in.query.dto.AdminScheduleInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.AdminScheduleInfo.AdminScheduleParticipantInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleBaseInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleBaseInfo.ScheduleAttendancePolicyInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleBaseInfo.ScheduleLocationInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleInfo;
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
public class ScheduleQueryService implements GetScheduleUseCase {

    private final LoadSchedulePort loadSchedulePort;
    private final LoadScheduleParticipantPort loadScheduleParticipantPort;

    // 내 일정 조회
    @Override
    public List<ScheduleInfo> searchMySchedules(
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

        // 해당 일정들의 참여자 전체 조회
        // 참여자 상세 정보 일괄 조회 후 scheduleId 기준으로 그룹화
        Map<Long, List<ScheduleParticipantDetailDto>> participantsMap = getParticipantsMap(scheduleIds);

        // 도메인 -> List<ScheduleInfo> 매핑
        return schedules.stream()
            .map(schedule -> {
                // 순회 중인 scheduleId를 key로 하여 참여자 목록 꺼냄, 참여자가 없으면 빈 리스트 반환
                List<ScheduleParticipantDetailDto> participants =
                    participantsMap.getOrDefault(schedule.getId(), List.of());

                return createScheduleInfo(schedule, participants, memberId);
            }).toList();
    }

    // 일정 상세 조회
    @Override
    public ScheduleInfo getScheduleDetails(Long scheduleId, Long memberId) {

        Schedule schedule = loadSchedulePort.findById(scheduleId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        List<ScheduleParticipantDetailDto> participants = loadScheduleParticipantPort.findParticipantDetailsByScheduleId(
            scheduleId);

        return createScheduleInfo(schedule, participants, memberId);
    }

    // [운영진용] 기간 기반 일정들의 출석 현황 조회
    // 본인이 참여하는 일정을 보여줍니다.
    // `attendanceStatus`를 통해서 요청 상태를 필터링할 수 있습니다.
    // 제공되지 않은 경우, 모든 상태에 대해서 반환합니다.
    // 조회 기간과 무관하게 과거 일정 중에서 출석을 승인하지 않은 일정은 계속 표시됩니다.
    @Override
    public List<AdminScheduleInfo> searchAdminSchedules(Instant from, Instant to,
                                                        AttendanceStatus attendanceStatus,
                                                        Long memberId) {
        // 일정 조회
        List<Schedule> schedules = loadSchedulePort.findAdminSchedules(from, to, attendanceStatus, memberId);

        if (schedules.isEmpty()) {
            return List.of();
        }

        List<Long> scheduleIds = schedules.stream().map(Schedule::getId).toList();

        // 해당 일정들의 참여자 전체 조회
        // 참여자 상세 정보 일괄 조회 후 scheduleId 기준으로 그룹화
        Map<Long, List<ScheduleParticipantDetailDto>> participantsMap = getParticipantsMap(scheduleIds);

        return schedules.stream()
            .map(schedule -> {
                // participantsMap에서 schedule의 id를 가지고 있는 ScheduleParticipantDetailDto의 리스트를 반환
                List<ScheduleParticipantDetailDto> participants = participantsMap.getOrDefault(schedule.getId(),
                    List.of());

                // 파라미터로 받은 attendanceStatus와 ScheduleParticipantDetailDto의 attendanceStatus가 일치하는 것만 필터링
                List<AdminScheduleParticipantInfo> adminParticipants = participants.stream()
                    // 파라미터 attendanceStatus == null이면 true로 filter 통과
                    .filter(p -> attendanceStatus == null | p.attendanceStatus() == attendanceStatus)
                    .map(p -> new AdminScheduleParticipantInfo(
                        p.memberId(), p.name(), p.nickname(),
                        p.schoolId(), p.schoolName(), p.profileImageUrl(),
                        p.attendanceStatus(), p.isLocationVerified(), p.excuseReason()
                    ))
                    .toList();

                return new AdminScheduleInfo(createBaseInfo(schedule), adminParticipants);
            })
            // 필터링 결과, 보여줄 참여자가 없는 일정은 목록에서 제외
            .filter(adminInfo -> !adminInfo.participants().isEmpty())
            .toList();
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

    // 일정들의 참여자 상세 정보 일괄 조회 후, scheduleId 기준으로 그룹화 하는 메서드
    private Map<Long, List<ScheduleParticipantDetailDto>> getParticipantsMap(List<Long> scheduleIds) {

        List<ScheduleParticipantDetailDto> allParticipants =
            loadScheduleParticipantPort.findParticipantDetailsByScheduleIds(scheduleIds);

        return allParticipants.stream()
            .collect(Collectors.groupingBy(ScheduleParticipantDetailDto::scheduleId));
    }
}
