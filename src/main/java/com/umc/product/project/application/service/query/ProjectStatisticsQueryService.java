package com.umc.product.project.application.service.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.project.application.port.in.query.GetProjectStatisticsUseCase;
import com.umc.product.project.application.port.in.query.dto.statistics.ChapterProjectStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ChapterProjectStatisticsSummaryInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingRoundStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMemberApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMemberStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectRoundMemberCountInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectRoundMemberStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundSchoolApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolMatchingStatisticsInfo;
import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMatchingRoundRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectStatisticsQueryService implements GetProjectStatisticsUseCase {

    private final LoadProjectStatisticsPort loadProjectStatisticsPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public ProjectStatisticsInfo getByProjectId(Long projectId) {
        ProjectStatisticsProjectRow project = loadProjectStatisticsPort.getProjectById(projectId);
        List<ProjectStatisticsMatchingRoundRow> rounds =
            loadProjectStatisticsPort.listMatchingRoundsByChapterId(project.chapterId());
        List<ProjectStatisticsMemberRow> members =
            sortMembers(loadProjectStatisticsPort.listActiveMembersByProjectId(projectId));
        List<ProjectStatisticsApplicationRow> applications =
            sortApplications(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(Set.of(projectId)));
        StatisticsPopulation population = resolvePopulation(List.of(project));

        return assembleProject(project, rounds, members, applications, population);
    }

    @Override
    public ChapterProjectStatisticsInfo getByChapterId(Long chapterId) {
        List<ProjectStatisticsProjectRow> projects = loadProjectStatisticsPort.listProjectsByChapterId(chapterId);
        if (projects.isEmpty()) {
            return new ChapterProjectStatisticsInfo(chapterId, List.of(), emptySummary());
        }

        Set<Long> projectIds = projects.stream()
            .map(ProjectStatisticsProjectRow::projectId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        List<ProjectStatisticsMatchingRoundRow> rounds =
            loadProjectStatisticsPort.listMatchingRoundsByChapterId(chapterId);
        List<ProjectStatisticsMemberRow> members =
            sortMembers(loadProjectStatisticsPort.listActiveMembersByChapterId(chapterId));
        List<ProjectStatisticsApplicationRow> applications =
            sortApplications(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(projectIds));
        StatisticsPopulation population = resolvePopulation(projects);

        Map<Long, List<ProjectStatisticsMemberRow>> membersByProject = members.stream()
            .collect(Collectors.groupingBy(
                ProjectStatisticsMemberRow::projectId,
                LinkedHashMap::new,
                Collectors.toList()
            ));
        Map<Long, List<ProjectStatisticsApplicationRow>> applicationsByProject = applications.stream()
            .collect(Collectors.groupingBy(
                ProjectStatisticsApplicationRow::projectId,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<ProjectStatisticsInfo> projectStatistics = projects.stream()
            .map(project -> assembleProject(
                project,
                rounds,
                membersByProject.getOrDefault(project.projectId(), List.of()),
                applicationsByProject.getOrDefault(project.projectId(), List.of()),
                population
            ))
            .toList();

        StatisticsContext chapterContext = buildContext(rounds, applications, members, population);
        return new ChapterProjectStatisticsInfo(
            chapterId,
            projectStatistics,
            new ChapterProjectStatisticsSummaryInfo(
                buildRoundApplicationStatistics(chapterContext),
                buildRoundSchoolApplicationStatistics(chapterContext),
                buildSchoolMatchingStatistics(chapterContext),
                buildProjectRoundStatistics(projects, chapterContext)
            )
        );
    }

    private ProjectStatisticsInfo assembleProject(
        ProjectStatisticsProjectRow project,
        List<ProjectStatisticsMatchingRoundRow> rounds,
        List<ProjectStatisticsMemberRow> members,
        List<ProjectStatisticsApplicationRow> applications,
        StatisticsPopulation population
    ) {
        Map<ProjectMemberKey, List<ProjectStatisticsApplicationRow>> applicationsByMember =
            groupApplications(applications);
        StatisticsContext context = buildContext(rounds, applications, members, population);

        return new ProjectStatisticsInfo(
            project.projectId(),
            members.stream()
                .map(member -> toMemberInfo(member, applicationsByMember.getOrDefault(
                    ProjectMemberKey.from(member), List.of())))
                .toList(),
            buildRoundApplicationStatistics(context),
            buildRoundSchoolApplicationStatistics(context)
        );
    }

    private StatisticsPopulation resolvePopulation(List<ProjectStatisticsProjectRow> projects) {
        Set<Long> eligibleMemberIds = projects.stream()
            .map(ProjectStatisticsProjectRow::chapterId)
            .filter(Objects::nonNull)
            .distinct()
            .flatMap(chapterId -> getChallengerUseCase.listByChapterId(chapterId).stream())
            .filter(this::isEligibleChallenger)
            .map(ChallengerInfo::memberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, Long> schoolIdByMemberId = eligibleMemberIds.isEmpty()
            ? Map.of()
            : getMemberUseCase.findAllSchoolIdsByIds(eligibleMemberIds);

        return new StatisticsPopulation(eligibleMemberIds, schoolIdByMemberId);
    }

    private boolean isEligibleChallenger(ChallengerInfo challenger) {
        // 지원 가능 모집단(지부 인원)은 활동 중인 챌린저만 집계한다. 수료/제명 인원은 제외.
        return challenger.challengerStatus() == ChallengerStatus.ACTIVE
            && challenger.part() != ChallengerPart.ADMIN
            && challenger.part() != ChallengerPart.PLAN;
    }

    private StatisticsContext buildContext(
        List<ProjectStatisticsMatchingRoundRow> rounds,
        List<ProjectStatisticsApplicationRow> applications,
        List<ProjectStatisticsMemberRow> members,
        StatisticsPopulation population
    ) {
        Set<ProjectMemberKey> activeMemberKeys = members.stream()
            .map(ProjectMemberKey::from)
            .collect(Collectors.toSet());

        List<ProjectStatisticsApplicationRow> eligibleApplications = applications.stream()
            .filter(application -> population.eligibleMemberIds().contains(application.applicantMemberId()))
            .toList();
        List<ProjectStatisticsApplicationRow> matchedApplications = eligibleApplications.stream()
            .filter(application -> application.status() == ProjectApplicationStatus.APPROVED)
            .filter(application -> activeMemberKeys.contains(ProjectMemberKey.from(application)))
            .toList();

        return new StatisticsContext(rounds, eligibleApplications, matchedApplications, population);
    }

    private List<RoundApplicationStatisticsInfo> buildRoundApplicationStatistics(StatisticsContext context) {
        Map<Long, Set<Long>> applicantsByRound = groupMemberIdsByRound(context.applications());
        Map<Long, Set<Long>> matchedByRound = groupMemberIdsByRound(context.matchedApplications());

        List<RoundApplicationStatisticsInfo> statistics = new ArrayList<>();
        Set<Long> cumulativeMatchedMemberIds = new HashSet<>();
        for (ProjectStatisticsMatchingRoundRow round : context.rounds()) {
            long availableMemberCount = Math.max(
                0L,
                context.population().eligibleMemberIds().size() - cumulativeMatchedMemberIds.size()
            );
            statistics.add(new RoundApplicationStatisticsInfo(
                toMatchingRoundInfo(round),
                applicantsByRound.getOrDefault(round.matchingRoundId(), Set.of()).size(),
                availableMemberCount
            ));
            cumulativeMatchedMemberIds.addAll(matchedByRound.getOrDefault(round.matchingRoundId(), Set.of()));
        }
        return statistics;
    }

    private List<RoundSchoolApplicationStatisticsInfo> buildRoundSchoolApplicationStatistics(
        StatisticsContext context
    ) {
        Map<Long, Map<Long, Set<Long>>> roundSchoolApplicants = new HashMap<>();
        for (ProjectStatisticsApplicationRow application : context.applications()) {
            Long schoolId = context.population().schoolIdByMemberId().get(application.applicantMemberId());
            if (schoolId == null) {
                continue;
            }
            roundSchoolApplicants
                .computeIfAbsent(application.matchingRoundId(), ignored -> new HashMap<>())
                .computeIfAbsent(schoolId, ignored -> new HashSet<>())
                .add(application.applicantMemberId());
        }

        return context.rounds().stream()
            .map(round -> new RoundSchoolApplicationStatisticsInfo(
                toMatchingRoundInfo(round),
                toSchoolApplicationStatistics(
                    roundSchoolApplicants.getOrDefault(round.matchingRoundId(), Map.of()))
            ))
            .toList();
    }

    private List<SchoolApplicationStatisticsInfo> toSchoolApplicationStatistics(
        Map<Long, Set<Long>> memberIdsBySchool
    ) {
        return memberIdsBySchool.entrySet().stream()
            .map(entry -> new SchoolApplicationStatisticsInfo(entry.getKey(), entry.getValue().size()))
            .sorted(Comparator
                .comparingLong(SchoolApplicationStatisticsInfo::applicantCount)
                .reversed()
                .thenComparing(SchoolApplicationStatisticsInfo::schoolId))
            .toList();
    }

    private List<SchoolMatchingStatisticsInfo> buildSchoolMatchingStatistics(StatisticsContext context) {
        Map<Long, Long> totalMemberCountBySchool = context.population().eligibleMemberIds().stream()
            .map(context.population().schoolIdByMemberId()::get)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<Long, Set<Long>> matchedMemberIdsBySchool = new HashMap<>();
        for (ProjectStatisticsApplicationRow application : context.matchedApplications()) {
            Long schoolId = context.population().schoolIdByMemberId().get(application.applicantMemberId());
            if (schoolId == null) {
                continue;
            }
            matchedMemberIdsBySchool
                .computeIfAbsent(schoolId, ignored -> new HashSet<>())
                .add(application.applicantMemberId());
        }

        return totalMemberCountBySchool.entrySet().stream()
            .map(entry -> new SchoolMatchingStatisticsInfo(
                entry.getKey(),
                matchedMemberIdsBySchool.getOrDefault(entry.getKey(), Set.of()).size(),
                entry.getValue()
            ))
            .sorted(Comparator.comparing(SchoolMatchingStatisticsInfo::schoolId))
            .toList();
    }

    private List<ProjectRoundMemberStatisticsInfo> buildProjectRoundStatistics(
        List<ProjectStatisticsProjectRow> projects,
        StatisticsContext context
    ) {
        Map<Long, Map<Long, Set<Long>>> projectRoundApplied = new HashMap<>();
        for (ProjectStatisticsApplicationRow application : context.applications()) {
            projectRoundApplied
                .computeIfAbsent(application.projectId(), ignored -> new HashMap<>())
                .computeIfAbsent(application.matchingRoundId(), ignored -> new HashSet<>())
                .add(application.applicantMemberId());
        }

        Map<Long, Map<Long, Set<Long>>> projectRoundMatched = new HashMap<>();
        for (ProjectStatisticsApplicationRow application : context.matchedApplications()) {
            projectRoundMatched
                .computeIfAbsent(application.projectId(), ignored -> new HashMap<>())
                .computeIfAbsent(application.matchingRoundId(), ignored -> new HashSet<>())
                .add(application.applicantMemberId());
        }

        return projects.stream()
            .sorted(Comparator.comparing(ProjectStatisticsProjectRow::projectId))
            .map(project -> new ProjectRoundMemberStatisticsInfo(
                project.projectId(),
                context.rounds().stream()
                    .map(round -> new ProjectRoundMemberCountInfo(
                        toMatchingRoundInfo(round),
                        projectRoundApplied
                            .getOrDefault(project.projectId(), Map.of())
                            .getOrDefault(round.matchingRoundId(), Set.of())
                            .size(),
                        projectRoundMatched
                            .getOrDefault(project.projectId(), Map.of())
                            .getOrDefault(round.matchingRoundId(), Set.of())
                            .size()
                    ))
                    .toList()
            ))
            .toList();
    }

    private Map<Long, Set<Long>> groupMemberIdsByRound(Collection<ProjectStatisticsApplicationRow> applications) {
        Map<Long, Set<Long>> memberIdsByRound = new HashMap<>();
        for (ProjectStatisticsApplicationRow application : applications) {
            memberIdsByRound
                .computeIfAbsent(application.matchingRoundId(), ignored -> new HashSet<>())
                .add(application.applicantMemberId());
        }
        return memberIdsByRound;
    }

    private Map<ProjectMemberKey, List<ProjectStatisticsApplicationRow>> groupApplications(
        Collection<ProjectStatisticsApplicationRow> applications
    ) {
        return sortApplications(applications).stream()
            .collect(Collectors.groupingBy(
                ProjectMemberKey::from,
                LinkedHashMap::new,
                Collectors.toList()
            ));
    }

    private List<ProjectStatisticsMemberRow> sortMembers(Collection<ProjectStatisticsMemberRow> members) {
        return members.stream()
            .sorted(Comparator
                .comparing(ProjectStatisticsMemberRow::projectId)
                .thenComparing(ProjectStatisticsMemberRow::projectMemberId))
            .toList();
    }

    private List<ProjectStatisticsApplicationRow> sortApplications(
        Collection<ProjectStatisticsApplicationRow> applications
    ) {
        return applications.stream()
            .sorted(Comparator
                .comparing(ProjectStatisticsApplicationRow::projectId)
                .thenComparing(ProjectStatisticsApplicationRow::applicantMemberId)
                .thenComparing(ProjectStatisticsApplicationRow::matchingRoundType)
                .thenComparing(ProjectStatisticsApplicationRow::matchingRoundPhase)
                .thenComparing(ProjectStatisticsApplicationRow::applicationId))
            .toList();
    }

    private ProjectMemberStatisticsInfo toMemberInfo(
        ProjectStatisticsMemberRow member,
        List<ProjectStatisticsApplicationRow> applications
    ) {
        return new ProjectMemberStatisticsInfo(
            member.projectMemberId(),
            member.memberId(),
            member.part(),
            member.status(),
            applications.stream()
                .map(this::toApplicationInfo)
                .toList()
        );
    }

    private ProjectMemberApplicationStatisticsInfo toApplicationInfo(ProjectStatisticsApplicationRow row) {
        return new ProjectMemberApplicationStatisticsInfo(
            row.applicationId(),
            row.status(),
            new ProjectMatchingRoundStatisticsInfo(
                row.matchingRoundId(),
                row.matchingRoundType(),
                row.matchingRoundPhase()
            )
        );
    }

    private ProjectMatchingRoundStatisticsInfo toMatchingRoundInfo(ProjectStatisticsMatchingRoundRow row) {
        return new ProjectMatchingRoundStatisticsInfo(
            row.matchingRoundId(),
            row.matchingRoundType(),
            row.matchingRoundPhase()
        );
    }

    private ChapterProjectStatisticsSummaryInfo emptySummary() {
        return new ChapterProjectStatisticsSummaryInfo(List.of(), List.of(), List.of(), List.of());
    }

    private record StatisticsPopulation(
        Set<Long> eligibleMemberIds,
        Map<Long, Long> schoolIdByMemberId
    ) {
    }

    private record StatisticsContext(
        List<ProjectStatisticsMatchingRoundRow> rounds,
        List<ProjectStatisticsApplicationRow> applications,
        List<ProjectStatisticsApplicationRow> matchedApplications,
        StatisticsPopulation population
    ) {
    }

    private record ProjectMemberKey(Long projectId, Long memberId) {

        private static ProjectMemberKey from(ProjectStatisticsMemberRow row) {
            return new ProjectMemberKey(row.projectId(), row.memberId());
        }

        private static ProjectMemberKey from(ProjectStatisticsApplicationRow row) {
            return new ProjectMemberKey(row.projectId(), row.applicantMemberId());
        }
    }
}
