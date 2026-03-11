package com.umc.product.schedule.application.service.query;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleListUseCase;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    private final GetStudyGroupUseCase getStudyGroupUseCase;

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

    /**
     * 역할에 따른 일정 목록 조회
     * <p>
     * 1. 중앙 운영사무국(총괄/부총괄/운영국원/교육국원): 해당 기수에서 본인이 포함된 일정 (본인의 AttendanceRecord가 있는 일정)
     * 2. 학교 회장단(회장/부회장): 같은 학교 챌린저가 생성한 모든 일정
     * 3. 파트장: 본인이 생성했거나 포함된 일정 + 담당 파트의 스터디 그룹 일정
     * 4. 그 외: 본인이 생성했거나 본인이 포함된 일정
     */
    private List<Schedule> resolveSchedulesByRole(Long memberId) {
        ChallengerInfoWithStatus currentChallenger =
            getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);

        Long gisuId = currentChallenger.gisuId();
        Long currentChallengerId = currentChallenger.challengerId();

        // 1. 중앙 운영사무국: 본인이 생성(출석부 있는)하거나 포함된 기수 내 일정
        if (getChallengerRoleUseCase.isCentralMemberInGisu(memberId, gisuId)) {
            return getMyAndCreatedSchedulesWithSheet(memberId, gisuId, currentChallengerId);
        }

        // 2. 학교 회장단: 같은 학교 챌린저들이 생성한 일정 (출석부 있는 것만)
        Long schoolId = getMemberUseCase.getMemberInfoById(memberId).schoolId();
        if (schoolId != null && getChallengerRoleUseCase.isSchoolCoreInGisu(memberId, gisuId, schoolId)) {
            List<ChallengerInfo> gisuChallengers = getChallengerUseCase.getByGisuId(gisuId);
            Set<Long> memberIds = gisuChallengers.stream()
                .map(ChallengerInfo::memberId)
                .collect(Collectors.toSet());
            Map<Long, MemberInfo> memberInfoMap = getMemberUseCase.getProfiles(memberIds);

            List<Long> schoolChallengerIds = gisuChallengers.stream()
                .filter(c -> {
                    MemberInfo info = memberInfoMap.get(c.memberId());
                    return info != null && schoolId.equals(info.schoolId());
                })
                .map(ChallengerInfo::challengerId)
                .toList();

            return loadSchedulePort.findWithSheetByAuthorChallengerIdIn(schoolChallengerIds);
        }

        // 3. 파트장: 본인이 생성했거나 포함된 일정 + 담당 파트의 스터디 그룹 일정 (중복 제거)
        Set<ChallengerPart> responsibleParts =
            getChallengerRoleUseCase.getResponsiblePartsByMemberAndGisu(memberId, gisuId);
        if (!responsibleParts.isEmpty()) {
            List<Schedule> mySchedules = getMyAndCreatedSchedules(memberId, gisuId, currentChallengerId);
            List<Long> studyGroupIds = getStudyGroupUseCase.getStudyGroupIdsByParts(gisuId, responsibleParts);
            List<Schedule> studyGroupSchedules = loadSchedulePort.findByStudyGroupIdIn(studyGroupIds);

            return Stream.concat(mySchedules.stream(), studyGroupSchedules.stream())
                .collect(Collectors.toMap(Schedule::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new))
                .values()
                .stream()
                .toList();
        }

        // 4. 그 외: 본인이 생성한 일정 + 본인이 포함된 일정 (중복 제거)
        return getMyAndCreatedSchedules(memberId, gisuId, currentChallengerId);
    }

    private List<Schedule> getMyAndCreatedSchedulesWithSheet(Long memberId, Long gisuId, Long currentChallengerId) {
        List<Schedule> createdByMeWithSheet = loadSchedulePort.findWithSheetByAuthorChallengerIdIn(List.of(currentChallengerId));
        List<Schedule> includingMe = loadSchedulePort.findMySchedulesByGisu(memberId, gisuId);

        return Stream.concat(createdByMeWithSheet.stream(), includingMe.stream())
            .collect(Collectors.toMap(Schedule::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new))
            .values()
            .stream()
            .toList();
    }

    private List<Schedule> getMyAndCreatedSchedules(Long memberId, Long gisuId, Long currentChallengerId) {
        List<Schedule> createdByMe = loadSchedulePort.findByAuthorChallengerIdIn(List.of(currentChallengerId));
        List<Schedule> includingMe = loadSchedulePort.findMySchedulesByGisu(memberId, gisuId);

        return Stream.concat(createdByMe.stream(), includingMe.stream())
            .collect(Collectors.toMap(Schedule::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new))
            .values()
            .stream()
            .toList();
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
