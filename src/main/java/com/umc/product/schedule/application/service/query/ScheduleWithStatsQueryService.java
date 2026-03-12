package com.umc.product.schedule.application.service.query;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleListUseCase;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendancesByScheduleInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithStatsInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.vo.AttendanceStats;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleWithStatsQueryService implements GetScheduleListUseCase {

    private final LoadSchedulePort loadSchedulePort;
    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public List<ScheduleWithStatsInfo> getAll(Long memberId) {
        Instant now = Instant.now();

        // 1. 역할 기반 일정 목록 조회
        List<Schedule> schedules = resolveSchedulesByRole(memberId);

        if (schedules.isEmpty()) {
            return List.of();
        }

        // 2. 일정 ID 목록 추출
        List<Long> scheduleIds = schedules.stream()
            .map(Schedule::getId)
            .toList();

        // 3. 해당 일정들의 출석부 조회
        List<AttendanceSheet> sheets = loadAttendanceSheetPort.findByScheduleIds(scheduleIds);
        Map<Long, AttendanceSheet> sheetByScheduleId = sheets.stream()
            .collect(Collectors.toMap(AttendanceSheet::getScheduleId, Function.identity()));

        // 4. 출석부 ID 목록 추출
        List<Long> sheetIds = sheets.stream()
            .map(AttendanceSheet::getId)
            .toList();

        // 5. 출석 기록 조회 및 출석부별로 그룹화
        Map<Long, List<AttendanceRecord>> recordsBySheetId = sheetIds.isEmpty()
            ? Map.of()
            : loadAttendanceRecordPort.findByAttendanceSheetIds(sheetIds).stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getAttendanceSheetId));

        // 6. 각 일정에 대해 통계 계산 및 Info 생성
        List<ScheduleWithStatsInfo> result = schedules.stream()
            .map(schedule -> {
                AttendanceSheet sheet = sheetByScheduleId.get(schedule.getId());
                AttendanceStats stats = calculateStats(sheet, recordsBySheetId);
                return ScheduleWithStatsInfo.of(schedule, sheet, stats, now);
            })
            .toList();

        // 7. "예정" 상태 제외 및 정렬: 진행 중 → 종료됨 (종료됨은 최근 것부터)
        //    같은 상태 내에서는 활성 출석부가 있는 일정 우선
        return result.stream()
            .filter(info -> !"예정".equals(info.status()))
            .sorted(scheduleComparator())
            .toList();
    }

    @Override
    public List<PendingAttendancesByScheduleInfo> getAllPendingByRole(Long memberId) {
        // 1. 역할 기반 일정 목록 조회
        List<Schedule> schedules = resolveSchedulesByRole(memberId);

        if (schedules.isEmpty()) {
            return List.of();
        }

        // 2. 일정 ID 목록 추출
        List<Long> scheduleIds = schedules.stream()
            .map(Schedule::getId)
            .toList();

        // 3. 해당 일정들의 출석부 조회
        List<AttendanceSheet> sheets = loadAttendanceSheetPort.findByScheduleIds(scheduleIds);

        if (sheets.isEmpty()) {
            return List.of();
        }

        // 4. 출석부 ID 목록 추출
        List<Long> sheetIds = sheets.stream()
            .map(AttendanceSheet::getId)
            .toList();

        // 5. 모든 출석부의 승인 대기 기록 일괄 조회 (N+1 방지)
        List<PendingAttendanceInfo> allPendingRecords =
            loadAttendanceRecordPort.findPendingWithMemberInfoBySheetIds(sheetIds);

        if (allPendingRecords.isEmpty()) {
            return List.of();
        }

        // 6. scheduleId별로 그룹핑
        Map<Long, List<PendingAttendanceInfo>> groupedBySchedule = allPendingRecords.stream()
            .collect(Collectors.groupingBy(PendingAttendanceInfo::scheduleId));

        // 7. Schedule 맵 생성
        Map<Long, Schedule> scheduleMap = schedules.stream()
            .collect(Collectors.toMap(Schedule::getId, Function.identity()));

        // 8. 결과 조합
        return groupedBySchedule.entrySet().stream()
            .map(entry -> {
                Long scheduleId = entry.getKey();
                List<PendingAttendanceInfo> pendingList = entry.getValue();
                Schedule schedule = scheduleMap.get(scheduleId);

                String scheduleName = schedule != null ? schedule.getName() : "알 수 없는 일정";

                return PendingAttendancesByScheduleInfo.of(scheduleId, scheduleName, pendingList);
            })
            .toList();
    }

    /**
     * 역할에 따른 일정 목록 조회 (우선순위 기반, 최상위 역할만 적용)
     * <p>
     * 1. 중앙운영사무국원: 해당 기수 출석부 중 본인 AttendanceRecord가 있는 일정 + 본인 생성 일정 2. 학교 회장단: 본인 학교 구성원이 파트장인 스터디 일정 + 본인 생성 일정 3. 학교
     * 파트장: 본인이 파트장인 스터디 그룹 일정 + 본인 생성 일정 4. 그 외 운영진: 본인 생성 일정만
     * <p>
     * 공통: requiresApproval=true 조건 포함
     */
    private List<Schedule> resolveSchedulesByRole(Long memberId) {
        ChallengerInfoWithStatus currentChallenger =
            getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);

        Long gisuId = currentChallenger.gisuId();
        Long currentChallengerId = currentChallenger.challengerId();

        // 1. 중앙운영사무국원: 본인 AttendanceRecord가 있는 일정 (생성자는 자동 포함)
        if (getChallengerRoleUseCase.isCentralMemberInGisu(memberId, gisuId)) {
            return loadSchedulePort.findSchedulesForCentralMember(memberId, gisuId);
        }

        // 2. 학교 회장단: 본인 학교 구성원이 파트장인 스터디 일정 + 본인 생성 일정
        Long schoolId = getMemberUseCase.getMemberInfoById(memberId).schoolId();
        if (schoolId != null && getChallengerRoleUseCase.isSchoolCoreInGisu(memberId, gisuId, schoolId)) {
            return loadSchedulePort.findSchedulesForSchoolCore(
                schoolId, gisuId, currentChallengerId);
        }

        // 3. 학교 파트장: 본인이 파트장인 스터디 그룹 일정 + 본인 생성 일정
        if (getChallengerRoleUseCase.hasRoleInGisu(memberId, gisuId, ChallengerRoleType.SCHOOL_PART_LEADER)) {
            return loadSchedulePort.findSchedulesForPartLeader(currentChallengerId, gisuId);
        }

        // 4. 그 외 운영진: 본인 생성 일정만
        return loadSchedulePort.findSchedulesByAuthor(currentChallengerId, gisuId);
    }

    private AttendanceStats calculateStats(AttendanceSheet sheet, Map<Long, List<AttendanceRecord>> recordsBySheetId) {
        if (sheet == null) {
            return new AttendanceStats(0, 0, 0);
        }

        List<AttendanceRecord> records = recordsBySheetId.getOrDefault(sheet.getId(), List.of());

        int totalCount = records.size();
        int presentCount = (int) records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.PRESENT
                || r.getStatus() == AttendanceStatus.LATE
                || r.getStatus() == AttendanceStatus.EXCUSED)
            .count();
        int pendingCount = (int) records.stream()
            .filter(r -> r.getStatus().isPending())
            .count();

        return new AttendanceStats(totalCount, presentCount, pendingCount);
    }

    private Comparator<ScheduleWithStatsInfo> scheduleComparator() {
        return (a, b) -> {
            int orderA = getStatusOrder(a.status());
            int orderB = getStatusOrder(b.status());

            if (orderA != orderB) {
                return Integer.compare(orderA, orderB);
            }

            // 같은 상태: 활성 출석부 우선
            if (a.sheetActive() != b.sheetActive()) {
                return a.sheetActive() ? -1 : 1;
            }

            // 진행 중은 시작 시간 오름차순, 종료됨은 종료 시간 내림차순
            if ("종료됨".equals(a.status())) {
                return b.endsAt().compareTo(a.endsAt());
            }
            return a.startsAt().compareTo(b.startsAt());
        };
    }

    private int getStatusOrder(String status) {
        return switch (status) {
            case "진행 중" -> 0;
            case "예정" -> 1;
            case "종료됨" -> 2;
            default -> 3;
        };
    }
}
