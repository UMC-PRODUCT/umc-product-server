package com.umc.product.project.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.project.application.port.in.query.GetMatchingStatisticsUseCase;
import com.umc.product.project.application.port.in.query.dto.RoundMemberInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.MatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectRoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundStat;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolStat;
import com.umc.product.project.application.port.out.LoadMatchingStatisticsPort;
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
public class MatchingStatisticsQueryService implements GetMatchingStatisticsUseCase {

    private final LoadMatchingStatisticsPort loadMatchingStatisticsPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;

    /**
     * 호출 횟수: DB 1회(listMembersByRound) + Challenger 1회 + Member 1회 = 3회.
     */
    @Override
    public MatchingStatisticsInfo getManagerStats(Long gisuId, Long chapterId) {
        List<RoundMemberInfo> entries =
            loadMatchingStatisticsPort.listMembersByRound(gisuId, chapterId);

        Set<Long> eligibleMemberIds = resolveEligibleMemberIds(gisuId);
        Map<Long, Long> memberSchoolMap = fetchSchoolMap(eligibleMemberIds, entries);
        Map<Long, Long> schoolTotals = computeSchoolTotals(eligibleMemberIds, memberSchoolMap);
        long totalQuota = schoolTotals.values().stream().mapToLong(Long::longValue).sum();

        return new MatchingStatisticsInfo(
            buildRoundStats(entries, totalQuota),
            buildSchoolStats(entries, memberSchoolMap, schoolTotals),
            buildProjectRoundStats(entries)
        );
    }

    private Set<Long> resolveEligibleMemberIds(Long gisuId) {
        return getChallengerUseCase.getAllByGisuId(gisuId).stream()
            .filter(c -> c.part() != ChallengerPart.ADMIN)
            .map(ChallengerInfo::memberId)
            .collect(Collectors.toSet());
    }

    private Map<Long, Long> fetchSchoolMap(Set<Long> eligibleMemberIds, List<RoundMemberInfo> entries) {
        Set<Long> allMemberIds = new HashSet<>(eligibleMemberIds);
        entries.stream().map(RoundMemberInfo::memberId).forEach(allMemberIds::add);
        return allMemberIds.isEmpty() ? Map.of() : getMemberUseCase.findAllSchoolIdsByIds(allMemberIds);
    }

    private Map<Long, Long> computeSchoolTotals(Set<Long> eligibleMemberIds, Map<Long, Long> memberSchoolMap) {
        return eligibleMemberIds.stream()
            .map(memberSchoolMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private List<RoundStat> buildRoundStats(List<RoundMemberInfo> entries, long totalQuota) {
        Map<Long, Long> roundCounts = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            roundCounts.merge(e.roundId(), 1L, Long::sum);
        }
        return roundCounts.entrySet().stream()
            .map(e -> new RoundStat(e.getKey(), e.getValue(), totalQuota))
            .toList();
    }

    private List<SchoolStat> buildSchoolStats(
        List<RoundMemberInfo> entries,
        Map<Long, Long> memberSchoolMap,
        Map<Long, Long> schoolTotals
    ) {
        Map<Long, Map<Long, Long>> schoolRoundCounts = new HashMap<>();
        for (RoundMemberInfo e : entries) {
            Long schoolId = memberSchoolMap.get(e.memberId());
            if (schoolId == null) continue;
            schoolRoundCounts
                .computeIfAbsent(schoolId, k -> new HashMap<>())
                .merge(e.roundId(), 1L, Long::sum);
        }

        List<SchoolStat> result = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Long>> schoolEntry : schoolRoundCounts.entrySet()) {
            Long schoolId = schoolEntry.getKey();
            long total = schoolTotals.getOrDefault(schoolId, 0L);
            for (Map.Entry<Long, Long> roundEntry : schoolEntry.getValue().entrySet()) {
                result.add(new SchoolStat(schoolId, roundEntry.getKey(), roundEntry.getValue(), total));
            }
        }
        return result;
    }

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
