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
import com.umc.product.project.application.port.in.query.dto.statistics.RoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolStat;
import com.umc.product.project.application.port.out.LoadApplicationStatisticsPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.ArrayList;
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
        List<RoundMemberInfo> entries =
            loadApplicationStatisticsPort.listApplicantsByRound(gisuId, chapterId);

        Map<Long, Long> memberSchoolMap = fetchSchoolMap(eligibleMemberIds, entries);
        Map<Long, Long> schoolTotals = computeSchoolTotals(eligibleMemberIds, memberSchoolMap);
        long totalQuota = schoolTotals.values().stream().mapToLong(Long::longValue).sum();

        return new ApplicationStatisticsInfo(
            buildRoundStats(entries, totalQuota),
            buildSchoolStats(entries, memberSchoolMap, schoolTotals),
            buildProjectRoundStats(entries)
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

        List<RoundMemberInfo> entries =
            loadApplicationStatisticsPort.listApplicantsByRoundForOwner(callerMemberId, gisuId, chapterId);

        Map<Long, Long> memberSchoolMap = fetchSchoolMap(eligibleMemberIds, entries);
        Map<Long, Long> schoolTotals = computeSchoolTotals(eligibleMemberIds, memberSchoolMap);
        long totalQuota = schoolTotals.values().stream().mapToLong(Long::longValue).sum();

        return new ApplicationStatisticsInfo(
            buildRoundStats(entries, totalQuota),
            buildSchoolStatsWithoutTotal(entries, memberSchoolMap),
            buildProjectRoundStats(entries)
        );
    }

    /**
     * gisuId 기준 ADMIN 파트 제외 챌린저의 memberId 집합을 반환한다. ADMIN 파트 운영진은 프로젝트 팀원으로 참여하지 않으므로 분모 계산에서 제외한다.
     */
    private Set<Long> resolveEligibleMemberIds(Long gisuId) {
        return getChallengerUseCase.getPartsByGisuId(gisuId).stream()
            .filter(c -> c.part() != ChallengerPart.ADMIN)
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
     * roundId별 고유 지원자 수를 집계해 RoundStat 목록을 반환한다. Set으로 중복 제거하는 이유: 한 지원자가 같은 차수에 여러 프로젝트에 지원한 경우 entries에 복수 row가 존재하기
     * 때문.
     */
    private List<RoundStat> buildRoundStats(List<RoundMemberInfo> entries, long totalQuota) {
        Map<Long, Set<Long>> roundApplicants = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            roundApplicants.computeIfAbsent(e.roundId(), k -> new HashSet<>()).add(e.memberId());
        }
        return roundApplicants.entrySet().stream()
            .map(e -> new RoundStat(e.getKey(), e.getValue().size(), totalQuota))
            .toList();
    }

    /**
     * schoolId × roundId별 고유 지원자 수를 집계해 SchoolStat 목록을 반환한다. total은 해당 학교의 전체 챌린저 수(분모)로, 차수와 무관하게 동일한 값이 반복된다. Set으로
     * 중복 제거하는 이유: 한 지원자가 같은 차수에 여러 프로젝트에 지원한 경우 중복 방지.
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

        List<SchoolStat> result = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Set<Long>>> schoolEntry : schoolRoundApplicants.entrySet()) {
            Long schoolId = schoolEntry.getKey();
            Long total = schoolTotals.getOrDefault(schoolId, 0L);
            for (Map.Entry<Long, Set<Long>> roundEntry : schoolEntry.getValue().entrySet()) {
                result.add(new SchoolStat(schoolId, roundEntry.getKey(), roundEntry.getValue().size(), total));
            }
        }
        return result;
    }

    /**
     * PM챌린저 경로 전용. buildSchoolStats와 동일하지만 total을 null로 반환한다. PM챌린저 뷰에서는 학교별 분모가 필요 없다.
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

        List<SchoolStat> result = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Set<Long>>> schoolEntry : schoolRoundApplicants.entrySet()) {
            Long schoolId = schoolEntry.getKey();
            for (Map.Entry<Long, Set<Long>> roundEntry : schoolEntry.getValue().entrySet()) {
                result.add(new SchoolStat(schoolId, roundEntry.getKey(), roundEntry.getValue().size(), null));
            }
        }
        return result;
    }

    /**
     * (projectId, roundId)별 지원자 수를 집계해 ProjectRoundStat 목록을 반환한다. 프로젝트별로는 지원자가 중복 지원할 수 없으므로 단순 count로 집계한다.
     */
    private List<ProjectRoundStat> buildProjectRoundStats(List<RoundMemberInfo> entries) {
        Map<Long, Map<Long, Long>> projectRoundCounts = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            projectRoundCounts
                .computeIfAbsent(e.projectId(), k -> new HashMap<>())
                .merge(e.roundId(), 1L, Long::sum);
        }
        return projectRoundCounts.entrySet().stream()
            .flatMap(e -> e.getValue().entrySet().stream()
                .map(re -> new ProjectRoundStat(e.getKey(), re.getKey(), re.getValue())))
            .toList();
    }
}
