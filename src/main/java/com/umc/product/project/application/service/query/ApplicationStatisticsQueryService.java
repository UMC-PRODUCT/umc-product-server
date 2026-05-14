package com.umc.product.project.application.service.query;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerPartInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.project.application.port.in.query.GetApplicationStatisticsUseCase;
import com.umc.product.project.application.port.in.query.dto.RoundMemberInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectRoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundCount;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolStat;
import com.umc.product.project.application.port.out.LoadApplicationStatisticsPort;
import com.umc.product.project.application.port.out.LoadMatchingStatisticsPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationStatisticsQueryService implements GetApplicationStatisticsUseCase {

    private final LoadApplicationStatisticsPort loadApplicationStatisticsPort;
    private final LoadMatchingStatisticsPort loadMatchingStatisticsPort;
    private final LoadProjectPort loadProjectPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetMemberUseCase getMemberUseCase;

    /**
     * 호출자 역할에 따라 운영진 또는 PM챌린저 경로로 분기한다. ChallengerRole 보유 여부로 운영진을 판단한다. ADMIN 파트 외에 프로젝트에 참여하는 운영진도 있으므로
     * ChallengerPart 대신 ChallengerRole 기준을 사용한다. - 운영진: ChallengerRole 1회 + Challenger 1회 + DB 1회 + Member 1회 = 4회 -
     * PM챌린저: ChallengerRole 1회 + Challenger 1회 + DB 2회(chapter 검증 + entries) + Member 1회 = 5회
     */
    @Override
    public ApplicationStatisticsInfo getStats(Long gisuId, Long chapterId, Long callerMemberId) {
        boolean isManager = getChallengerRoleUseCase
            .hasAnyRoleTypeInGisu(callerMemberId, gisuId, ChallengerRoleType.values());

        Set<Long> eligibleMemberIds = resolveEligibleMemberIds(gisuId);

        if (isManager) {
            return buildManagerStats(gisuId, chapterId, eligibleMemberIds);
        }
        return buildPmStats(callerMemberId, gisuId, chapterId, eligibleMemberIds);
    }

    /**
     * 운영진 경로: gisuId + chapterId 범위 전체 프로젝트 기준으로 집계. SchoolStat.total(분모)은 eligibleMemberIds 기반으로 계산한다.
     */
    private ApplicationStatisticsInfo buildManagerStats(
        Long gisuId, Long chapterId, Set<Long> eligibleMemberIds
    ) {
        List<RoundMemberInfo> appEntries =
            loadApplicationStatisticsPort.listApplicantsByRound(gisuId, chapterId);
        List<RoundMemberInfo> matchEntries =
            loadMatchingStatisticsPort.getMembersByRound(gisuId, chapterId);

        Map<Long, Long> memberSchoolMap = fetchSchoolMap(eligibleMemberIds, appEntries);
        Map<Long, Long> schoolTotals = computeSchoolTotals(eligibleMemberIds, memberSchoolMap);
        long totalEligible = schoolTotals.values().stream().mapToLong(Long::longValue).sum();

        return new ApplicationStatisticsInfo(
            buildRoundStats(appEntries, matchEntries, totalEligible),
            buildSchoolStats(appEntries, memberSchoolMap, schoolTotals),
            buildProjectRoundStats(appEntries)
        );
    }

    /**
     * PM챌린저 경로: 호출자 소유 프로젝트만 scope. SchoolStat.total은 null로 반환한다. chapterId 검증에 실패하면 403 예외를 발생시킨다.
     */
    private ApplicationStatisticsInfo buildPmStats(
        Long callerMemberId, Long gisuId, Long chapterId, Set<Long> eligibleMemberIds
    ) {
        if (!loadProjectPort.existsByOwnerAndGisuAndChapter(callerMemberId, gisuId, chapterId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        List<RoundMemberInfo> appEntries =
            loadApplicationStatisticsPort.listApplicantsByRoundForOwner(callerMemberId, gisuId, chapterId);
        List<RoundMemberInfo> matchEntries =
            loadMatchingStatisticsPort.getMembersByRoundForOwner(callerMemberId, gisuId, chapterId);

        Map<Long, Long> memberSchoolMap = fetchSchoolMap(eligibleMemberIds, appEntries);
        Map<Long, Long> schoolTotals = computeSchoolTotals(eligibleMemberIds, memberSchoolMap);
        long totalEligible = schoolTotals.values().stream().mapToLong(Long::longValue).sum();

        return new ApplicationStatisticsInfo(
            buildRoundStats(appEntries, matchEntries, totalEligible),
            buildSchoolStatsWithoutTotal(appEntries, memberSchoolMap),
            null
        );
    }

    /**
     * gisuId 기준 ADMIN·PLAN 파트 제외 챌린저의 memberId 집합을 반환한다. ADMIN 운영진은 팀원으로 참여하지 않고,
     * PLAN(PM)은 프로젝트 오너로서 매칭 대상이 아니므로 분모 계산에서 제외한다.
     */
    private Set<Long> resolveEligibleMemberIds(Long gisuId) {
        return getChallengerUseCase.getPartsByGisuId(gisuId).stream()
            .filter(c -> c.part() != ChallengerPart.ADMIN && c.part() != ChallengerPart.PLAN)
            .map(ChallengerPartInfo::memberId)
            .collect(Collectors.toSet());
    }

    /**
     * 챌린저 memberIds + 지원자 memberIds를 합산해 단일 getMemberUseCase 호출로 memberId → schoolId 맵을 반환한다. 두 집합을 합쳐 한 번만 호출함으로써 크로스
     * 도메인 호출을 최소화한다.
     */
    private Map<Long, Long> fetchSchoolMap(Set<Long> eligibleMemberIds, List<RoundMemberInfo> entries) {
        Set<Long> allMemberIds = new HashSet<>(eligibleMemberIds);
        entries.stream().map(RoundMemberInfo::memberId).forEach(allMemberIds::add);
        return allMemberIds.isEmpty() ? Map.of() : getMemberUseCase.findAllSchoolIdsByIds(allMemberIds);
    }

    /**
     * eligibleMemberIds 기준으로 schoolId → 총원(분모)을 계산한다. 학교에 소속되지 않은 멤버(schoolId = null)는 집계에서 제외한다.
     */
    private Map<Long, Long> computeSchoolTotals(Set<Long> eligibleMemberIds, Map<Long, Long> memberSchoolMap) {
        return eligibleMemberIds.stream()
            .map(memberSchoolMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    /**
     * roundId별 고유 지원자 수를 집계해 RoundStat 목록을 반환한다.
     * quota는 차수별 슬라이딩 분모: 총 인원 - (이전 차수까지 누적 매칭 인원).
     * 분모는 매칭 기준이므로 matchEntries를 별도로 받아 계산한다.
     * Set으로 중복 제거하는 이유: 한 지원자가 같은 차수에 여러 프로젝트에 지원할 수 있어 entries에 복수 row가 존재하기 때문.
     */
    private List<RoundStat> buildRoundStats(
        List<RoundMemberInfo> appEntries,
        List<RoundMemberInfo> matchEntries,
        long totalEligible
    ) {
        Map<Long, Set<Long>> roundApplicants = new HashMap<>();
        for (RoundMemberInfo e : appEntries) {
            roundApplicants.computeIfAbsent(e.roundId(), k -> new HashSet<>()).add(e.memberId());
        }

        Map<Long, Set<Long>> roundMatched = new HashMap<>();
        for (RoundMemberInfo e : matchEntries) {
            roundMatched.computeIfAbsent(e.roundId(), k -> new HashSet<>()).add(e.memberId());
        }

        List<RoundStat> result = new ArrayList<>();
        Set<Long> cumulativeMatched = new HashSet<>();
        for (Long roundId : roundApplicants.keySet().stream().sorted().toList()) {
            long quota = totalEligible - cumulativeMatched.size();
            result.add(new RoundStat(roundId, roundApplicants.get(roundId).size(), quota));
            cumulativeMatched.addAll(roundMatched.getOrDefault(roundId, Set.of()));
        }
        return result;
    }

    /**
     * 학교별로 그룹핑한 뒤 차수별 고유 지원자 수를 중첩 구조로 반환한다.
     * Set으로 중복 제거하는 이유: 한 지원자가 같은 차수에 여러 프로젝트에 지원한 경우 중복 방지.
     */
    private List<SchoolStat> buildSchoolStats(
        List<RoundMemberInfo> entries,
        Map<Long, Long> memberSchoolMap,
        Map<Long, Long> schoolTotals
    ) {
        Map<Long, Map<Long, Set<Long>>> schoolRoundApplicants = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            Long schoolId = memberSchoolMap.get(e.memberId());
            if (schoolId == null) {
                continue;
            }
            schoolRoundApplicants
                .computeIfAbsent(schoolId, k -> new HashMap<>())
                .computeIfAbsent(e.roundId(), k -> new HashSet<>())
                .add(e.memberId());
        }

        return schoolRoundApplicants.entrySet().stream()
            .map(schoolEntry -> {
                Long schoolId = schoolEntry.getKey();
                List<RoundCount> rounds = schoolEntry.getValue().entrySet().stream()
                    .map(re -> new RoundCount(re.getKey(), re.getValue().size()))
                    .sorted(Comparator.comparing(RoundCount::roundId))
                    .toList();
                return new SchoolStat(schoolId, schoolTotals.getOrDefault(schoolId, 0L), rounds);
            })
            .sorted(Comparator.comparing(SchoolStat::schoolId))
            .toList();
    }

    /**
     * PM챌린저 경로 전용. buildSchoolStats와 동일하지만 total을 null로 반환한다.
     */
    private List<SchoolStat> buildSchoolStatsWithoutTotal(
        List<RoundMemberInfo> entries,
        Map<Long, Long> memberSchoolMap
    ) {
        Map<Long, Map<Long, Set<Long>>> schoolRoundApplicants = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            Long schoolId = memberSchoolMap.get(e.memberId());
            if (schoolId == null) {
                continue;
            }
            schoolRoundApplicants
                .computeIfAbsent(schoolId, k -> new HashMap<>())
                .computeIfAbsent(e.roundId(), k -> new HashSet<>())
                .add(e.memberId());
        }

        return schoolRoundApplicants.entrySet().stream()
            .map(schoolEntry -> {
                List<RoundCount> rounds = schoolEntry.getValue().entrySet().stream()
                    .map(re -> new RoundCount(re.getKey(), re.getValue().size()))
                    .sorted(Comparator.comparing(RoundCount::roundId))
                    .toList();
                return new SchoolStat(schoolEntry.getKey(), null, rounds);
            })
            .sorted(Comparator.comparing(SchoolStat::schoolId))
            .toList();
    }

    /**
     * 프로젝트별로 그룹핑한 뒤 차수별 지원자 수를 중첩 구조로 반환한다.
     * 프로젝트별로는 지원자가 중복 지원할 수 없으므로 단순 count로 집계한다.
     */
    private List<ProjectRoundStat> buildProjectRoundStats(List<RoundMemberInfo> entries) {
        Map<Long, Map<Long, Long>> projectRoundCounts = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            projectRoundCounts
                .computeIfAbsent(e.projectId(), k -> new HashMap<>())
                .merge(e.roundId(), 1L, Long::sum);
        }
        return projectRoundCounts.entrySet().stream()
            .map(e -> {
                List<RoundCount> rounds = e.getValue().entrySet().stream()
                    .map(re -> new RoundCount(re.getKey(), re.getValue()))
                    .sorted(Comparator.comparing(RoundCount::roundId))
                    .toList();
                return new ProjectRoundStat(e.getKey(), rounds);
            })
            .sorted(Comparator.comparing(ProjectRoundStat::projectId))
            .toList();
    }
}
