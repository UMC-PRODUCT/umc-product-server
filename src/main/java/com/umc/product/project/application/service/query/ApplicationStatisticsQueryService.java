package com.umc.product.project.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.project.application.port.in.query.GetApplicationStatisticsUseCase;
import com.umc.product.project.application.port.in.query.dto.RoundMemberInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.MyApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectRoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolStat;
import com.umc.product.project.application.port.out.LoadApplicationStatisticsPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
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
    private final GetMemberUseCase getMemberUseCase;

    /**
     * 호출 횟수: DB 1회(listApplicantsByRound) + Challenger 1회 + Member 1회 = 3회.
     */
    @Override
    public ApplicationStatisticsInfo getManagerStats(Long gisuId, Long chapterId) {
        List<RoundMemberInfo> entries =
            loadApplicationStatisticsPort.listApplicantsByRound(gisuId, chapterId);

        Set<Long> eligibleMemberIds = resolveEligibleMemberIds(gisuId);
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
     * 호출 횟수: DB 2회(getById + listApplicantsByRoundForProject) + Challenger 1회 + Member 1회 = 4회.
     */
    @Override
    public MyApplicationStatisticsInfo getMyStats(Long projectId) {
        Long gisuId = loadProjectPort.getById(projectId).getGisuId();

        List<RoundMemberInfo> entries =
            loadApplicationStatisticsPort.listApplicantsByRoundForProject(projectId);

        Set<Long> eligibleMemberIds = resolveEligibleMemberIds(gisuId);
        Map<Long, Long> memberSchoolMap = fetchSchoolMap(eligibleMemberIds, entries);
        Map<Long, Long> schoolTotals = computeSchoolTotals(eligibleMemberIds, memberSchoolMap);
        long totalQuota = schoolTotals.values().stream().mapToLong(Long::longValue).sum();

        return new MyApplicationStatisticsInfo(
            buildRoundStats(entries, totalQuota),
            buildSchoolStats(entries, memberSchoolMap, schoolTotals)
        );
    }

    /** gisuId 기준 ADMIN 제외 챌린저의 memberId 집합. */
    private Set<Long> resolveEligibleMemberIds(Long gisuId) {
        return getChallengerUseCase.getAllByGisuId(gisuId).stream()
            .filter(c -> c.part() != ChallengerPart.ADMIN)
            .map(ChallengerInfo::memberId)
            .collect(Collectors.toSet());
    }

    /** 챌린저 + 지원자 memberIds를 합산해 단일 호출로 memberId → schoolId 맵을 반환한다. */
    private Map<Long, Long> fetchSchoolMap(Set<Long> eligibleMemberIds, List<RoundMemberInfo> entries) {
        Set<Long> allMemberIds = new HashSet<>(eligibleMemberIds);
        entries.stream().map(RoundMemberInfo::memberId).forEach(allMemberIds::add);
        return allMemberIds.isEmpty() ? Map.of() : getMemberUseCase.findAllSchoolIdsByIds(allMemberIds);
    }

    /** eligibleMemberIds 기준 schoolId → 총원(분모). */
    private Map<Long, Long> computeSchoolTotals(Set<Long> eligibleMemberIds, Map<Long, Long> memberSchoolMap) {
        return eligibleMemberIds.stream()
            .map(memberSchoolMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    /** entries → roundId별 고유 지원자 수 집계 → RoundStat 목록. */
    private List<RoundStat> buildRoundStats(List<RoundMemberInfo> entries, long totalQuota) {
        Map<Long, Set<Long>> roundApplicants = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            roundApplicants.computeIfAbsent(e.roundId(), k -> new HashSet<>()).add(e.memberId());
        }
        return roundApplicants.entrySet().stream()
            .map(e -> new RoundStat(e.getKey(), e.getValue().size(), totalQuota))
            .toList();
    }

    /** entries → schoolId × roundId별 지원자 수 집계 → SchoolStat 목록. */
    private List<SchoolStat> buildSchoolStats(
        List<RoundMemberInfo> entries,
        Map<Long, Long> memberSchoolMap,
        Map<Long, Long> schoolTotals
    ) {
        Map<Long, Map<Long, Set<Long>>> schoolRoundApplicants = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            Long schoolId = memberSchoolMap.get(e.memberId());
            if (schoolId == null) continue;
            schoolRoundApplicants
                .computeIfAbsent(schoolId, k -> new HashMap<>())
                .computeIfAbsent(e.roundId(), k -> new HashSet<>())
                .add(e.memberId());
        }

        List<SchoolStat> result = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Set<Long>>> schoolEntry : schoolRoundApplicants.entrySet()) {
            Long schoolId = schoolEntry.getKey();
            long total = schoolTotals.getOrDefault(schoolId, 0L);
            for (Map.Entry<Long, Set<Long>> roundEntry : schoolEntry.getValue().entrySet()) {
                result.add(new SchoolStat(schoolId, roundEntry.getKey(), roundEntry.getValue().size(), total));
            }
        }
        return result;
    }

    /** entries → (projectId, roundId)별 지원자 수 집계 → ProjectRoundStat 목록. */
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
