package com.umc.product.project.application.service.query;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerPartInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.project.application.port.in.query.GetMatchingStatisticsUseCase;
import com.umc.product.project.application.port.in.query.dto.RoundMemberInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.MatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicantStatistics;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicantStatistics.ProjectApplicantCountPerRound;
import com.umc.product.project.application.port.in.query.dto.statistics.MatchingRoundStatistics;
import com.umc.product.project.application.port.in.query.dto.statistics.ApplicantSchoolStatistics;
import com.umc.product.project.application.port.out.LoadMatchingStatisticsPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.ArrayList;
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
public class MatchingStatisticsQueryService implements GetMatchingStatisticsUseCase {

    private final LoadMatchingStatisticsPort loadMatchingStatisticsPort;
    private final LoadProjectPort loadProjectPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetMemberUseCase getMemberUseCase;

    /**
     * 호출자 역할에 따라 운영진 또는 PM챌린저 경로로 분기한다. ChallengerRole 보유 여부로 운영진을 판단한다. ADMIN 파트 외에 프로젝트에 참여하는 운영진도 있으므로
     * ChallengerRole 기준을 사용한다.
     */
    @Override
    public MatchingStatisticsInfo getStats(Long gisuId, Long chapterId, Long callerMemberId) {
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
    private MatchingStatisticsInfo buildManagerStats(
        Long gisuId, Long chapterId, Set<Long> eligibleMemberIds
    ) {
        List<RoundMemberInfo> entries =
            loadMatchingStatisticsPort.getMembersByRound(gisuId, chapterId).stream()
                .filter(e -> eligibleMemberIds.contains(e.memberId())).toList();

        Map<Long, Long> memberSchoolMap = fetchSchoolMap(eligibleMemberIds, entries);
        Map<Long, Long> schoolTotals = computeSchoolTotals(eligibleMemberIds, memberSchoolMap);
        long totalEligible = schoolTotals.values().stream().mapToLong(Long::longValue).sum();

        return new MatchingStatisticsInfo(
            buildRoundStats(entries, totalEligible),
            buildSchoolStats(entries, memberSchoolMap, schoolTotals),
            buildProjectRoundStats(entries)
        );
    }

    /**
     * PM챌린저 경로: 호출자 소유 프로젝트만 scope. SchoolStat.total은 null로 반환한다. chapterId 검증에 실패하면 403 예외를 발생시킨다.
     */
    private MatchingStatisticsInfo buildPmStats(
        Long callerMemberId, Long gisuId, Long chapterId, Set<Long> eligibleMemberIds
    ) {
        if (!loadProjectPort.existsByOwnerAndGisuAndChapter(callerMemberId, gisuId, chapterId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        List<RoundMemberInfo> entries =
            loadMatchingStatisticsPort.getMembersByRoundForOwner(callerMemberId, gisuId, chapterId).stream()
                .filter(e -> eligibleMemberIds.contains(e.memberId())).toList();

        Map<Long, Long> memberSchoolMap = fetchSchoolMap(eligibleMemberIds, entries);
        Map<Long, Long> schoolTotals = computeSchoolTotals(eligibleMemberIds, memberSchoolMap);
        long totalEligible = schoolTotals.values().stream().mapToLong(Long::longValue).sum();

        return new MatchingStatisticsInfo(
            buildRoundStats(entries, totalEligible),
            buildSchoolStatsWithoutTotal(entries, memberSchoolMap),
            null
        );
    }

    /**
     * gisuId 기준 ADMIN·PLAN 파트 제외 챌린저의 memberId 집합을 반환한다. ADMIN 운영진은 팀원으로 참여하지 않고, PLAN(PM)은 프로젝트 오너로서 매칭 대상이 아니므로 분모
     * 계산에서 제외한다.
     */
    private Set<Long> resolveEligibleMemberIds(Long gisuId) {
        return getChallengerUseCase.getPartsByGisuId(gisuId).stream()
            .filter(c -> c.part() != ChallengerPart.ADMIN && c.part() != ChallengerPart.PLAN)
            .map(ChallengerPartInfo::memberId)
            .collect(Collectors.toSet());
    }

    /**
     * 챌린저 memberIds + 매칭 멤버 memberIds를 합산해 단일 getMemberUseCase 호출로 memberId → schoolId 맵을 반환한다. 두 집합을 합쳐 한 번만 호출함으로써
     * 크로스 도메인 호출을 최소화한다.
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
     * roundId별 매칭 인원 수를 집계해 RoundStat 목록을 반환한다. quota는 차수별 슬라이딩 분모: 총 인원 - (이전 차수까지 누적 매칭 인원). 매칭된 멤버는 pool에서 영구 이탈하므로
     * 이전 차수 매칭 인원을 누적 차감한다.
     */
    private List<MatchingRoundStatistics> buildRoundStats(List<RoundMemberInfo> entries, long totalEligible) {
        Map<Long, Set<Long>> roundMatched = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            roundMatched.computeIfAbsent(e.roundId(), k -> new HashSet<>()).add(e.memberId());
        }

        List<MatchingRoundStatistics> result = new ArrayList<>();
        long remaining = totalEligible;
        for (Long roundId : roundMatched.keySet().stream().sorted().toList()) {
            Set<Long> matched = roundMatched.get(roundId);
            result.add(new MatchingRoundStatistics(roundId, matched.size(), remaining));
            remaining -= matched.size();
        }
        return result;
    }

    /**
     * 학교별로 그룹핑한 뒤 차수별 매칭 인원 수를 중첩 구조로 반환한다.
     */
    private List<ApplicantSchoolStatistics> buildSchoolStats(
        List<RoundMemberInfo> entries,
        Map<Long, Long> memberSchoolMap,
        Map<Long, Long> schoolTotals
    ) {
        Map<Long, Map<Long, Long>> schoolRoundCounts = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            Long schoolId = memberSchoolMap.get(e.memberId());
            if (schoolId == null) {
                continue;
            }
            schoolRoundCounts
                .computeIfAbsent(schoolId, k -> new HashMap<>())
                .merge(e.roundId(), 1L, Long::sum);
        }

        return schoolRoundCounts.entrySet().stream()
            .map(schoolEntry -> {
                Long schoolId = schoolEntry.getKey();
                List<ProjectApplicantCountPerRound> rounds = schoolEntry.getValue().entrySet().stream()
                    .map(re -> new ProjectApplicantCountPerRound(re.getKey(), re.getValue()))
                    .sorted(Comparator.comparing(ProjectApplicantCountPerRound::matchingRoundId))
                    .toList();
                return new ApplicantSchoolStatistics(schoolId, schoolTotals.getOrDefault(schoolId, 0L), rounds);
            })
            .sorted(Comparator.comparing(ApplicantSchoolStatistics::schoolId))
            .toList();
    }

    /**
     * PM챌린저 경로 전용. buildSchoolStats와 동일하지만 total을 null로 반환한다.
     */
    private List<ApplicantSchoolStatistics> buildSchoolStatsWithoutTotal(
        List<RoundMemberInfo> entries,
        Map<Long, Long> memberSchoolMap
    ) {
        Map<Long, Map<Long, Long>> schoolRoundCounts = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            Long schoolId = memberSchoolMap.get(e.memberId());
            if (schoolId == null) {
                continue;
            }
            schoolRoundCounts
                .computeIfAbsent(schoolId, k -> new HashMap<>())
                .merge(e.roundId(), 1L, Long::sum);
        }

        return schoolRoundCounts.entrySet().stream()
            .map(schoolEntry -> {
                List<ProjectApplicantCountPerRound> rounds = schoolEntry.getValue().entrySet().stream()
                    .map(re -> new ProjectApplicantCountPerRound(re.getKey(), re.getValue()))
                    .sorted(Comparator.comparing(ProjectApplicantCountPerRound::matchingRoundId))
                    .toList();
                return new ApplicantSchoolStatistics(schoolEntry.getKey(), null, rounds);
            })
            .sorted(Comparator.comparing(ApplicantSchoolStatistics::schoolId))
            .toList();
    }

    /**
     * 프로젝트별로 그룹핑한 뒤 차수별 매칭 인원 수를 중첩 구조로 반환한다. 한 멤버는 프로젝트당 하나의 ProjectMember만 존재하므로 단순 count로 집계한다.
     */
    private List<ProjectApplicantStatistics> buildProjectRoundStats(List<RoundMemberInfo> entries) {
        Map<Long, Map<Long, Long>> projectRoundCounts = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            projectRoundCounts
                .computeIfAbsent(e.projectId(), k -> new HashMap<>())
                .merge(e.roundId(), 1L, Long::sum);
        }
        return projectRoundCounts.entrySet().stream()
            .map(e -> {
                List<ProjectApplicantCountPerRound> rounds = e.getValue().entrySet().stream()
                    .map(re -> new ProjectApplicantCountPerRound(re.getKey(), re.getValue()))
                    .sorted(Comparator.comparing(ProjectApplicantCountPerRound::matchingRoundId))
                    .toList();
                return new ProjectApplicantStatistics(e.getKey(), rounds);
            })
            .sorted(Comparator.comparing(ProjectApplicantStatistics::projectId))
            .toList();
    }
}
