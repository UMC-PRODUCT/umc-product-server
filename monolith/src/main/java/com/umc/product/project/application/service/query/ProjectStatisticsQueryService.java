package com.umc.product.project.application.service.query;

import java.time.Clock;
import java.time.Instant;
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
import java.util.Optional;
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
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;
import com.umc.product.project.application.port.in.query.GetProjectStatisticsUseCase;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.PhaseApplicantCountInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.SchoolStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.SummaryInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingRoundStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingStatisticsInfo.PartStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingStatisticsInfo.PhaseMatchingCountInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMemberApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMemberStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundSchoolApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolApplicationStatisticsInfo;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.application.port.out.dto.ProjectMemberMatchedRoundInfo;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMatchedMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsTargetProjectRow;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.application.service.policy.ProjectStatisticsAccessPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectStatisticsQueryService implements GetProjectStatisticsUseCase {

    private static final List<MatchingPhase> APPLICATION_PHASES =
        List.of(MatchingPhase.FIRST, MatchingPhase.SECOND, MatchingPhase.THIRD);

    private final LoadProjectStatisticsPort loadProjectStatisticsPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final ProjectStatisticsAccessPolicy projectStatisticsAccessPolicy;
    private final ProjectMatchingChapterResolver matchingChapterResolver;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final GetChapterUseCase getChapterUseCase;
    private final GetSchoolUseCase getSchoolUseCase;
    private final Clock clock;

    @Override
    public ProjectStatisticsInfo getByProjectId(Long projectId, Long requesterMemberId) {
        Project target = loadProjectPort.getById(projectId);
        validateProjectAccess(requesterMemberId, target);

        ProjectStatisticsProjectRow project = loadProjectStatisticsPort.getProjectById(projectId);
        List<ProjectMatchingRound> rounds = loadProjectMatchingRoundPort.listByChapterId(project.chapterId()).stream()
            .sorted(Comparator.comparing(ProjectMatchingRound::getStartsAt).thenComparing(ProjectMatchingRound::getId))
            .toList();
        List<ProjectStatisticsMemberRow> members =
            sortMembers(loadProjectStatisticsPort.listActiveMembersByProjectId(projectId));
        List<ProjectStatisticsApplicationRow> applications =
            sortApplications(loadProjectStatisticsPort.listCountedApplicationsByProjectIds(Set.of(projectId)));
        StatisticsPopulation population = resolvePopulation(List.of(project));

        return assembleProject(project, rounds, members, applications, population);
    }

    @Override
    public ProjectApplicationStatisticsInfo getApplicationStatisticsByChapterId(
        Long chapterId, Long requesterMemberId
    ) {
        validateChapterAccess(requesterMemberId, chapterId);
        Set<Long> projectIds = loadProjectStatisticsPort.listProjectsByChapterId(chapterId).stream()
            .map(ProjectStatisticsProjectRow::projectId).collect(Collectors.toSet());
        return assembleApplicationStatistics(chapterId, projectIds, projectIds);
    }

    @Override
    public ProjectApplicationStatisticsInfo getApplicationStatisticsByProjectIds(
        Collection<Long> projectIds, Long requesterMemberId
    ) {
        if (projectIds == null || projectIds.isEmpty() || projectIds.stream().anyMatch(Objects::isNull)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_STATISTICS_INVALID_TARGET);
        }
        Set<Long> distinctIds = new HashSet<>(projectIds);
        List<Project> projects = loadProjectPort.listByIds(distinctIds);
        if (projects.size() != distinctIds.size()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        for (Project project : projects) {
            validateProjectAccess(requesterMemberId, project);
        }
        Set<Long> chapterIds = projects.stream().map(Project::getChapterId).collect(Collectors.toSet());
        Set<Long> gisuIds = projects.stream().map(Project::getGisuId).collect(Collectors.toSet());
        if (chapterIds.size() != 1 || chapterIds.contains(null) || gisuIds.size() != 1) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_STATISTICS_SCOPE_MISMATCH);
        }
        Long chapterId = chapterIds.iterator().next();
        Set<Long> chapterProjectIds = loadProjectStatisticsPort.listProjectsByChapterId(chapterId).stream()
            .map(ProjectStatisticsProjectRow::projectId).collect(Collectors.toSet());
        return assembleApplicationStatistics(chapterId, distinctIds, chapterProjectIds);
    }

    @Override
    public ProjectMatchingStatisticsInfo getMatchingStatisticsByChapterId(Long chapterId) {
        List<ProjectMatchingRound> allRounds = loadProjectMatchingRoundPort.listByChapterId(chapterId);
        Optional<ProjectMatchingRound> selected = selectMatchingRound(allRounds, clock.instant());
        if (selected.isEmpty()) {
            return ProjectMatchingStatisticsInfo.empty(chapterId);
        }
        MatchingType type = selected.get().getType();
        List<ProjectMatchingRound> rounds = allRounds.stream()
            .filter(round -> round.getType() == type)
            .toList();
        Long gisuId = getChapterUseCase.getGisuIdByChapterId(chapterId);
        List<ProjectStatisticsTargetProjectRow> projects =
            loadProjectStatisticsPort.listPublicProjectsByChapterId(chapterId);
        StatisticsPopulation population = resolvePopulation(chapterId, gisuId, type);
        List<MatchingPhase> phases = rounds.stream()
            .map(ProjectMatchingRound::getPhase)
            .distinct()
            .sorted()
            .toList();
        Map<Long, MatchingPhase> phaseByRoundId = rounds.stream()
            .collect(Collectors.toMap(ProjectMatchingRound::getId, ProjectMatchingRound::getPhase));
        List<ProjectStatisticsMatchedMemberRow> members =
            loadProjectStatisticsPort.listPublicActiveMembersByChapterId(chapterId).stream()
                .filter(member -> phaseByRoundId.containsKey(member.matchingRoundId()))
                .filter(member -> population.eligibleMemberIds().contains(member.memberId()))
                .toList();
        Set<Long> projectIds = projects.stream()
            .map(ProjectStatisticsTargetProjectRow::projectId)
            .collect(Collectors.toSet());
        Map<Long, List<ProjectPartQuota>> quotasByProjectId = projectIds.isEmpty() ? Map.of()
            : loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(projectIds);

        return new ProjectMatchingStatisticsInfo(
            chapterId,
            buildRoundMatchingStatistics(phases, members, phaseByRoundId, population),
            buildPublicSchoolMatchingStatistics(chapterId, gisuId, members, population),
            buildProjectMatchingStatistics(projects, type, phases, members, phaseByRoundId, quotasByProjectId)
        );
    }

    /**
     * 기준 매칭 유형의 1·2·3차 지원 통계를 조립한다.
     * <p>
     * 지원 인원은 {@code requestedProjectIds} 범위에서 집계하고, 지원 가능 인원의 이전 차수 매칭 인원은
     * {@code chapterProjectIds}(지부 전체) 기준으로 차감한다. 프로젝트를 지정해 조회해도 분모가 지부 기준으로 유지되도록 두 범위를 분리한다.
     * 임시저장·승인대기 프로젝트는 지원서를 받을 수 없으므로 프로젝트별 통계에서 제외한다.
     */
    private ProjectApplicationStatisticsInfo assembleApplicationStatistics(
        Long chapterId, Set<Long> requestedProjectIds, Set<Long> chapterProjectIds
    ) {
        List<ProjectMatchingRound> allRounds = loadProjectMatchingRoundPort.listByChapterId(chapterId);
        Optional<ProjectMatchingRound> selected = selectMatchingRound(allRounds, clock.instant());
        if (selected.isEmpty()) {
            return ProjectApplicationStatisticsInfo.empty(chapterId);
        }
        List<ProjectStatisticsTargetProjectRow> targetProjects =
            loadProjectStatisticsPort.listApplicationStatisticsProjectsByIds(requestedProjectIds);
        ProjectMatchingRound matchingRound = selected.get();
        List<ProjectMatchingRound> rounds = allRounds.stream()
            .filter(round -> round.getType() == matchingRound.getType())
            .filter(round -> APPLICATION_PHASES.contains(round.getPhase()))
            .sorted(Comparator.comparing(ProjectMatchingRound::getPhase))
            .toList();
        Long gisuId = getChapterUseCase.getGisuIdByChapterId(chapterId);
        StatisticsPopulation population = resolvePopulation(chapterId, gisuId, matchingRound.getType());
        Set<Long> projectIds = targetProjects.stream()
            .map(ProjectStatisticsTargetProjectRow::projectId).collect(Collectors.toSet());
        Set<Long> roundIds = rounds.stream().map(ProjectMatchingRound::getId).collect(Collectors.toSet());
        List<ProjectStatisticsApplicationRow> applications = projectIds.isEmpty() ? List.of()
            : loadProjectStatisticsPort.listCountedApplicationsByProjectIds(projectIds).stream()
                .filter(application -> roundIds.contains(application.matchingRoundId()))
                .filter(application -> population.eligibleMemberIds().contains(application.applicantMemberId()))
                .toList();

        // 프로젝트 지정 조회에서도 분모는 지부 전체의 실제 배정 차수를 기준으로 계산한다.
        List<ProjectMemberMatchedRoundInfo> assignments =
            chapterProjectIds.isEmpty() || population.eligibleMemberIds().isEmpty() ? List.of()
                : loadProjectMemberPort.listMatchedRoundsByProjectIdsAndMemberIds(
                    chapterProjectIds, population.eligibleMemberIds());

        return new ProjectApplicationStatisticsInfo(chapterId, toMatchingRoundInfo(matchingRound), new SummaryInfo(
            buildRoundStatistics(rounds, applications, assignments, population),
            buildSchoolStatistics(chapterId, gisuId, rounds, applications, population),
            targetProjects.stream()
                .map(project -> new ProjectApplicationStatisticsInfo.ProjectStatisticsInfo(
                    project.projectId(), project.projectName(),
                    countByPhase(rounds, applications.stream()
                        .filter(application -> Objects.equals(application.projectId(), project.projectId()))
                        .toList())))
                .toList()
        ));
    }

    /**
     * 통계 기준 차수를 선택한다. 지원 기간(startsAt~endsAt)인 1~3차를 우선하고, 없으면 가장 최근에 종료된 1~3차를 사용한다.
     * 랜덤 매칭은 지원서를 받지 않으므로 선택 대상에서 제외한다.
     */
    private Optional<ProjectMatchingRound> selectMatchingRound(List<ProjectMatchingRound> rounds, Instant now) {
        List<ProjectMatchingRound> applicationRounds = rounds.stream()
            .filter(round -> APPLICATION_PHASES.contains(round.getPhase()))
            .toList();
        Optional<ProjectMatchingRound> active = applicationRounds.stream()
            .filter(round -> !round.getStartsAt().isAfter(now) && !round.getEndsAt().isBefore(now))
            .max(Comparator.comparing(ProjectMatchingRound::getStartsAt).thenComparing(ProjectMatchingRound::getId));
        return active.or(() -> applicationRounds.stream()
            .filter(round -> round.getEndsAt().isBefore(now))
            .max(Comparator.comparing(ProjectMatchingRound::getEndsAt).thenComparing(ProjectMatchingRound::getId)));
    }

    /**
     * 통계 대상 인원. 매칭 지부 기준 챌린저 중 해당 기수에서 활동 중(ACTIVE)이고 기준 매칭 유형에 해당하는 파트만 포함한다.
     */
    private StatisticsPopulation resolvePopulation(Long chapterId, Long gisuId, MatchingType type) {
        Set<Long> memberIds = listChallengersInMatchingChapter(chapterId).stream()
            .filter(challenger -> Objects.equals(challenger.gisuId(), gisuId))
            .filter(challenger -> challenger.challengerStatus() == ChallengerStatus.ACTIVE)
            .filter(challenger -> MatchingType.fromPart(challenger.part()).filter(type::equals).isPresent())
            .map(ChallengerInfo::memberId).filter(Objects::nonNull).collect(Collectors.toSet());
        return new StatisticsPopulation(memberIds, memberIds.isEmpty() ? Map.of()
            : getMemberUseCase.findAllSchoolIdsByIds(memberIds));
    }

    /**
     * 차수별 지원 인원과 지원 가능 인원을 계산한다.
     * <p>
     * 지원 가능 인원은 지원 가능 인원 전체에서 이전 차수로 실제 배정된 ACTIVE 팀원을 뺀 값이다.
     * 결과 공개 전(PENDING_PUBLICATION) 팀원은 아직 배정이 확정되지 않았으므로 차감하지 않는다.
     */
    private List<RoundApplicationStatisticsInfo> buildRoundStatistics(
        List<ProjectMatchingRound> rounds,
        List<ProjectStatisticsApplicationRow> applications,
        List<ProjectMemberMatchedRoundInfo> assignments,
        StatisticsPopulation population
    ) {
        Set<Long> roundIds = rounds.stream().map(ProjectMatchingRound::getId).collect(Collectors.toSet());
        return rounds.stream().map(round -> {
            long previouslyMatchedCount = assignments.stream()
                .filter(assignment -> roundIds.contains(assignment.matchingRoundId()))
                .filter(assignment -> assignment.matchingRoundPhase().compareTo(round.getPhase()) < 0)
                .map(ProjectMemberMatchedRoundInfo::memberId)
                .filter(population.eligibleMemberIds()::contains).distinct().count();
            long applicantCount = applications.stream()
                .filter(application -> Objects.equals(application.matchingRoundId(), round.getId()))
                .map(ProjectStatisticsApplicationRow::applicantMemberId).distinct().count();
            return new RoundApplicationStatisticsInfo(toMatchingRoundInfo(round), applicantCount,
                Math.max(0L, population.eligibleMemberIds().size() - previouslyMatchedCount));
        }).toList();
    }

    /**
     * 학교별 지원 가능 총원과 1·2·3차 지원 인원을 계산한다.
     * <p>
     * 지부 소속 학교는 지원자가 없어도 포함하고, 다른 지부에서 이동해 온 챌린저의 학교는 학교명을 별도로 조회해 추가한다.
     * 지원 가능 총원은 차수에 따라 차감하지 않는다.
     */
    private List<SchoolStatisticsInfo> buildSchoolStatistics(
        Long chapterId, Long gisuId, List<ProjectMatchingRound> rounds,
        List<ProjectStatisticsApplicationRow> applications, StatisticsPopulation population
    ) {
        Map<Long, Long> totals = population.eligibleMemberIds().stream().map(population.schoolIdByMemberId()::get)
            .filter(Objects::nonNull).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<Long, String> schoolNames = resolveSchoolNames(chapterId, gisuId, totals.keySet());
        return schoolNames.entrySet().stream().sorted(Map.Entry.comparingByKey())
            .map(school -> new SchoolStatisticsInfo(school.getKey(), school.getValue(),
                totals.getOrDefault(school.getKey(), 0L), countByPhase(rounds, applications.stream()
                    .filter(application -> Objects.equals(
                        population.schoolIdByMemberId().get(application.applicantMemberId()), school.getKey()))
                    .toList())))
            .toList();
    }

    /**
     * 차수별 지원 회원 수를 계산한다. 같은 차수에 여러 번 지원한 회원은 한 번만 세고, 지원자가 없는 차수는 0으로 반환한다.
     */
    private List<PhaseApplicantCountInfo> countByPhase(
        List<ProjectMatchingRound> rounds, List<ProjectStatisticsApplicationRow> applications
    ) {
        Map<MatchingPhase, Set<Long>> memberIdsByPhase = new HashMap<>();
        for (ProjectStatisticsApplicationRow application : applications) {
            memberIdsByPhase
                .computeIfAbsent(application.matchingRoundPhase(), ignored -> new HashSet<>())
                .add(application.applicantMemberId());
        }
        return rounds.stream()
            .map(ProjectMatchingRound::getPhase)
            .distinct()
            .map(phase -> new PhaseApplicantCountInfo(
                phase,
                memberIdsByPhase.getOrDefault(phase, Set.of()).size()
            ))
            .toList();
    }

    private Map<Long, String> resolveSchoolNames(Long chapterId, Long gisuId, Set<Long> schoolIds) {
        Map<Long, String> schoolNames = getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId).stream()
            .filter(chapter -> Objects.equals(chapter.chapterId(), chapterId))
            .flatMap(chapter -> chapter.schools().stream())
            .collect(Collectors.toMap(ChapterWithSchoolsInfo.SchoolInfo::schoolId,
                ChapterWithSchoolsInfo.SchoolInfo::schoolName));
        Set<Long> transferredSchoolIds = new HashSet<>(schoolIds);
        transferredSchoolIds.removeAll(schoolNames.keySet());
        if (!transferredSchoolIds.isEmpty()) {
            for (SchoolDetailInfo school : getSchoolUseCase.listDetailsByIds(transferredSchoolIds)) {
                schoolNames.put(school.schoolId(), school.schoolName());
            }
        }
        return schoolNames;
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

    private ProjectMatchingRoundStatisticsInfo toMatchingRoundInfo(ProjectMatchingRound round) {
        return new ProjectMatchingRoundStatisticsInfo(round.getId(), round.getType(), round.getPhase());
    }

    /**
     * 단건 프로젝트 통계 접근 권한 검증. 본인 프로젝트의 PO/Sub-PM 이면 통과, 아니면 지부 운영진 판정으로 위임한다.
     */
    private void validateProjectAccess(Long memberId, Project project) {
        if (!projectStatisticsAccessPolicy.canReadProjectStatistics(memberId, project)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
    }

    /**
     * 지부 단위 통계 접근 권한 검증. SUPER_ADMIN / 총괄단 / 해당 지부장 / 해당 지부 소속 학교 회장·부회장이면 통과,
     * 그 외에는 {@code PROJECT_ACCESS_DENIED}.
     * <p>
     * 요청 chapterId 는 치환하지 않고 통과/거부만 판정한다(총괄단의 타 지부 조회 보장).
     */
    private void validateChapterAccess(Long memberId, Long chapterId) {
        if (!projectStatisticsAccessPolicy.canReadChapterStatistics(memberId, chapterId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
    }

    private ProjectStatisticsInfo assembleProject(
        ProjectStatisticsProjectRow project,
        List<ProjectMatchingRound> rounds,
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
            .flatMap(chapterId -> listChallengersInMatchingChapter(chapterId).stream())
            .filter(this::isEligibleChallenger)
            .map(ChallengerInfo::memberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, Long> schoolIdByMemberId = eligibleMemberIds.isEmpty()
            ? Map.of()
            : getMemberUseCase.findAllSchoolIdsByIds(eligibleMemberIds);

        return new StatisticsPopulation(eligibleMemberIds, schoolIdByMemberId);
    }

    /**
     * 매칭 지부 기준 인원. 학교 기준 지부 인원에서 다른 지부로 이동한 챌린저를 빼고, 이 지부로 이동해 온 챌린저를 더한다.
     * <p>
     * 통계는 모집단 밖 지원자의 지원서를 버리므로({@code buildContext}), 이 보정이 없으면 이동한 챌린저의 지원서가 이동지부 통계에서 빠지고
     * 원지부 통계에는 미매칭으로 남는다.
     */
    private List<ChallengerInfo> listChallengersInMatchingChapter(Long chapterId) {
        return matchingChapterResolver.deltaOf(chapterId)
            .apply(getChallengerUseCase.listByChapterId(chapterId));
    }

    private boolean isEligibleChallenger(ChallengerInfo challenger) {
        // 지원 가능 모집단(지부 인원)은 활동 중인 챌린저만 집계한다. 수료/제명 인원은 제외.
        return challenger.challengerStatus() == ChallengerStatus.ACTIVE
            && challenger.part() != ChallengerPart.ADMIN
            && challenger.part() != ChallengerPart.PLAN;
    }

    private StatisticsContext buildContext(
        List<ProjectMatchingRound> rounds,
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

        return new StatisticsContext(rounds, eligibleApplications, matchedApplications, members, population);
    }

    private List<RoundApplicationStatisticsInfo> buildRoundApplicationStatistics(StatisticsContext context) {
        Map<Long, Long> applicationCountByRound = countApplicationsByRound(context.applications());
        Map<Long, Set<Long>> matchedByRound = groupMemberIdsByRound(context.matchedApplications());

        List<RoundApplicationStatisticsInfo> statistics = new ArrayList<>();
        Set<Long> cumulativeMatchedMemberIds = new HashSet<>();
        for (ProjectMatchingRound round : context.rounds()) {
            long availableMemberCount = Math.max(
                0L,
                context.population().eligibleMemberIds().size() - cumulativeMatchedMemberIds.size()
            );
            statistics.add(new RoundApplicationStatisticsInfo(
                toMatchingRoundInfo(round),
                applicationCountByRound.getOrDefault(round.getId(), 0L),
                availableMemberCount
            ));
            cumulativeMatchedMemberIds.addAll(matchedByRound.getOrDefault(round.getId(), Set.of()));
        }
        return statistics;
    }

    private List<RoundSchoolApplicationStatisticsInfo> buildRoundSchoolApplicationStatistics(
        StatisticsContext context
    ) {
        Map<Long, Map<Long, Set<Long>>> roundSchoolApplicants = new HashMap<>();
        for (ProjectStatisticsApplicationRow application : context.applications()) {
            if (application.applicantMemberId() == null) {
                continue;
            }
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
                    roundSchoolApplicants.getOrDefault(round.getId(), Map.of()))
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

    /**
     * 차수별 매칭 완료 인원과 매칭 대기 인원을 계산한다. 매칭 대기 인원은 이전 차수까지 매칭된 회원을 뺀 값이다.
     * 매칭 인원이 없는 차수도 0명으로 반환한다.
     */
    private List<ProjectMatchingStatisticsInfo.PhaseStatisticsInfo> buildRoundMatchingStatistics(
        List<MatchingPhase> phases,
        List<ProjectStatisticsMatchedMemberRow> members,
        Map<Long, MatchingPhase> phaseByRoundId,
        StatisticsPopulation population
    ) {
        Map<MatchingPhase, Set<Long>> memberIdsByPhase = groupMatchedMemberIdsByPhase(members, phaseByRoundId);
        List<ProjectMatchingStatisticsInfo.PhaseStatisticsInfo> statistics = new ArrayList<>();
        Set<Long> cumulativeMatchedMemberIds = new HashSet<>();
        for (MatchingPhase phase : phases) {
            Set<Long> matchedMemberIds = memberIdsByPhase.getOrDefault(phase, Set.of());
            long availableMemberCount = Math.max(
                0L,
                population.eligibleMemberIds().size() - cumulativeMatchedMemberIds.size()
            );
            statistics.add(new ProjectMatchingStatisticsInfo.PhaseStatisticsInfo(
                phase,
                matchedMemberIds.size(),
                availableMemberCount
            ));
            cumulativeMatchedMemberIds.addAll(matchedMemberIds);
        }
        return statistics;
    }

    private List<ProjectMatchingStatisticsInfo.SchoolStatisticsInfo> buildPublicSchoolMatchingStatistics(
        Long chapterId,
        Long gisuId,
        List<ProjectStatisticsMatchedMemberRow> members,
        StatisticsPopulation population
    ) {
        Map<Long, Long> totalMemberCountBySchool = population.eligibleMemberIds().stream()
            .map(population.schoolIdByMemberId()::get)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<Long, String> schoolNames = resolveSchoolNames(chapterId, gisuId, totalMemberCountBySchool.keySet());
        Map<Long, Set<Long>> matchedMemberIdsBySchool = new HashMap<>();
        for (ProjectStatisticsMatchedMemberRow member : members) {
            Long schoolId = population.schoolIdByMemberId().get(member.memberId());
            if (schoolId != null) {
                matchedMemberIdsBySchool.computeIfAbsent(schoolId, ignored -> new HashSet<>()).add(member.memberId());
            }
        }
        return schoolNames.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(school -> new ProjectMatchingStatisticsInfo.SchoolStatisticsInfo(
                school.getKey(),
                school.getValue(),
                matchedMemberIdsBySchool.getOrDefault(school.getKey(), Set.of()).size(),
                totalMemberCountBySchool.getOrDefault(school.getKey(), 0L)
            ))
            .toList();
    }

    /**
     * 프로젝트별 누적 매칭 인원과 차수별·파트별 매칭 인원, 모집 정원(TO)을 계산한다.
     * 정원은 기준 매칭 유형 파트에 등록된 전체 정원이며 잔여 인원이 아니다.
     */
    private List<ProjectMatchingStatisticsInfo.ProjectStatisticsInfo> buildProjectMatchingStatistics(
        List<ProjectStatisticsTargetProjectRow> projects,
        MatchingType type,
        List<MatchingPhase> phases,
        List<ProjectStatisticsMatchedMemberRow> members,
        Map<Long, MatchingPhase> phaseByRoundId,
        Map<Long, List<ProjectPartQuota>> quotasByProjectId
    ) {
        Map<Long, List<ProjectStatisticsMatchedMemberRow>> membersByProjectId = members.stream()
            .collect(Collectors.groupingBy(ProjectStatisticsMatchedMemberRow::projectId));

        return projects.stream()
            .map(project -> toProjectMatchingStatistics(
                project,
                type,
                phases,
                membersByProjectId.getOrDefault(project.projectId(), List.of()),
                phaseByRoundId,
                quotasByProjectId.getOrDefault(project.projectId(), List.of())
            ))
            .toList();
    }

    private ProjectMatchingStatisticsInfo.ProjectStatisticsInfo toProjectMatchingStatistics(
        ProjectStatisticsTargetProjectRow project,
        MatchingType type,
        List<MatchingPhase> phases,
        List<ProjectStatisticsMatchedMemberRow> members,
        Map<Long, MatchingPhase> phaseByRoundId,
        List<ProjectPartQuota> quotas
    ) {
        Map<ChallengerPart, Long> quotasByPart = quotas.stream()
            .filter(quota -> MatchingType.fromPart(quota.getPart()).filter(type::equals).isPresent())
            .collect(Collectors.toMap(ProjectPartQuota::getPart, ProjectPartQuota::getQuota));
        long totalQuota = quotasByPart.values().stream().mapToLong(Long::longValue).sum();
        List<PhaseMatchingCountInfo> roundStatistics =
            buildProjectRoundMatchingStatistics(phases, members, phaseByRoundId);
        List<PartStatisticsInfo> partStatistics = buildPartMatchingStatistics(members, quotasByPart);

        return new ProjectMatchingStatisticsInfo.ProjectStatisticsInfo(
            project.projectId(),
            project.projectName(),
            countMatchedMembers(members),
            totalQuota,
            roundStatistics,
            partStatistics
        );
    }

    private List<PhaseMatchingCountInfo> buildProjectRoundMatchingStatistics(
        List<MatchingPhase> phases,
        List<ProjectStatisticsMatchedMemberRow> members,
        Map<Long, MatchingPhase> phaseByRoundId
    ) {
        Map<MatchingPhase, Set<Long>> memberIdsByPhase = groupMatchedMemberIdsByPhase(members, phaseByRoundId);
        return phases.stream()
            .map(phase -> new PhaseMatchingCountInfo(
                phase,
                memberIdsByPhase.getOrDefault(phase, Set.of()).size()
            ))
            .toList();
    }

    private List<PartStatisticsInfo> buildPartMatchingStatistics(
        List<ProjectStatisticsMatchedMemberRow> members,
        Map<ChallengerPart, Long> quotasByPart
    ) {
        Map<ChallengerPart, Set<Long>> memberIdsByPart = new HashMap<>();
        for (ProjectStatisticsMatchedMemberRow member : members) {
            memberIdsByPart.computeIfAbsent(member.part(), ignored -> new HashSet<>()).add(member.memberId());
        }
        Set<ChallengerPart> parts = new HashSet<>(quotasByPart.keySet());
        parts.addAll(memberIdsByPart.keySet());

        return parts.stream()
            .sorted()
            .map(part -> new PartStatisticsInfo(
                part,
                memberIdsByPart.getOrDefault(part, Set.of()).size(),
                quotasByPart.getOrDefault(part, 0L)
            ))
            .toList();
    }

    private Map<MatchingPhase, Set<Long>> groupMatchedMemberIdsByPhase(
        List<ProjectStatisticsMatchedMemberRow> members,
        Map<Long, MatchingPhase> phaseByRoundId
    ) {
        Map<MatchingPhase, Set<Long>> memberIdsByPhase = new HashMap<>();
        for (ProjectStatisticsMatchedMemberRow member : members) {
            MatchingPhase phase = phaseByRoundId.get(member.matchingRoundId());
            memberIdsByPhase.computeIfAbsent(phase, ignored -> new HashSet<>()).add(member.memberId());
        }
        return memberIdsByPhase;
    }

    private long countMatchedMembers(Collection<ProjectStatisticsMatchedMemberRow> members) {
        return members.stream()
            .map(ProjectStatisticsMatchedMemberRow::memberId)
            .distinct()
            .count();
    }

    private Map<Long, Long> countApplicationsByRound(Collection<ProjectStatisticsApplicationRow> applications) {
        return applications.stream()
            .collect(Collectors.groupingBy(
                ProjectStatisticsApplicationRow::matchingRoundId,
                Collectors.counting()
            ));
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

    private record StatisticsPopulation(
        Set<Long> eligibleMemberIds,
        Map<Long, Long> schoolIdByMemberId
    ) {
    }

    private record StatisticsContext(
        List<ProjectMatchingRound> rounds,
        List<ProjectStatisticsApplicationRow> applications,
        List<ProjectStatisticsApplicationRow> matchedApplications,
        List<ProjectStatisticsMemberRow> members,
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
