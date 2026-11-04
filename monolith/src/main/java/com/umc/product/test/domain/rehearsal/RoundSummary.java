package com.umc.product.test.domain.rehearsal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.test.domain.rehearsal.ChapterRoles.ReservedSeat;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ApplicationRow;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.Candidate;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.PartSlot;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ProjectSlot;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.Round;

/**
 * 지원 수는 제출한 지원서만 센다(임시저장 제외).
 */
public record RoundSummary(
    Header round,
    List<ProjectRow> projects,
    List<PartHeadcount> parts,
    List<ReservedRow> qaReserved,
    List<Long> underfilledProjects,
    List<Long> projectsWithoutApplicants
) {
    public RoundSummary {
        projects = List.copyOf(projects);
        parts = List.copyOf(parts);
        qaReserved = List.copyOf(qaReserved);
        underfilledProjects = List.copyOf(underfilledProjects);
        projectsWithoutApplicants = List.copyOf(projectsWithoutApplicants);
    }

    public record Header(
        Long roundId,
        String name,
        Long chapterId,
        String chapter,
        MatchingType type,
        MatchingPhase phase,
        Instant startsAt,
        Instant endsAt,
        Instant decisionDeadline,
        Instant resultPublishAt,
        RoundStage stage,
        boolean autoDecided,
        boolean published,
        Instant observedAt
    ) {
    }

    public record ProjectRow(Long projectId, String name, String owner, boolean qaProject, List<PartRow> parts) {
        public ProjectRow {
            parts = List.copyOf(parts);
        }
    }

    /**
     * @param selected 이 차수 자동 선발이 팀원으로 넣은 수(발표 대기 포함)
     * @param fillRate (기존 팀원 + 이 차수 합격) / TO. 자동 선발 뒤에는 합격 대신 선발 수를 쓴다
     */
    public record PartRow(
        ChallengerPart part,
        int quota,
        int existingMembers,
        int selected,
        int appliedBot,
        int appliedQa,
        int approved,
        int rejected,
        int undecided,
        double fillRate
    ) {
        boolean underfilled() {
            return fillRate < 1.0;
        }
    }

    public record PartHeadcount(ChallengerPart part, int eligible, int withTeam, int withoutTeam, int applied,
                                int notApplied) {
    }

    /**
     * @param applicationStatus 이 차수 지원서 상태. 없으면 null
     */
    public record ReservedRow(String member, char label, ChallengerPart part, boolean hasTeam,
                              ProjectApplicationStatus applicationStatus, Long projectId) {
    }

    public static RoundSummary of(RoundSnapshot snapshot, ChapterRoles roles) {
        Round round = snapshot.round();
        RoundTimes times = round.times();
        Set<ChallengerPart> parts = round.parts();
        Map<Long, List<ApplicationRow>> applicationsByProject = snapshot.applications().stream()
            .collect(Collectors.groupingBy(ApplicationRow::projectId));

        List<ProjectRow> projects = new ArrayList<>();
        List<Long> underfilled = new ArrayList<>();
        List<Long> withoutApplicants = new ArrayList<>();
        for (ProjectSlot project : snapshot.projects().stream()
            .sorted(Comparator.comparing(ProjectSlot::projectId)).toList()) {
            List<ApplicationRow> applications = applicationsByProject.getOrDefault(project.projectId(), List.of());
            List<PartRow> rows = parts.stream().sorted()
                .filter(part -> project.parts().containsKey(part))
                .map(part -> partRow(part, project.parts().get(part), applications, times.autoDecided()))
                .toList();
            if (rows.isEmpty()) {
                continue;
            }
            projects.add(new ProjectRow(project.projectId(), project.name(), project.ownerEmail(),
                roles.qaProjectOwnerEmails().contains(project.ownerEmail()), rows));
            if (rows.stream().anyMatch(PartRow::underfilled)) {
                underfilled.add(project.projectId());
            }
            if (rows.stream().allMatch(row -> row.appliedBot() + row.appliedQa() == 0)) {
                withoutApplicants.add(project.projectId());
            }
        }

        Header header = new Header(times.roundId(), round.name(), round.chapterId(), round.chapterName(),
            round.type(), times.phase(), times.startsAt(), times.endsAt(), times.decisionDeadline(),
            times.resultPublishAt(), times.stageAt(snapshot.observedAt()), times.autoDecided(), times.published(),
            snapshot.observedAt());
        return new RoundSummary(header, projects, headcounts(snapshot, parts), reservedRows(snapshot, roles),
            underfilled, withoutApplicants);
    }

    private static PartRow partRow(ChallengerPart part, PartSlot slot, List<ApplicationRow> applications,
                                   boolean autoDecided) {
        List<ApplicationRow> submitted = applications.stream()
            .filter(application -> application.part() == part && application.submitted()).toList();
        int bot = (int) submitted.stream().filter(ApplicationRow::botAnswered).count();
        int approved = count(submitted, ProjectApplicationStatus.APPROVED);
        int filled = slot.existingMembers() + (autoDecided ? slot.selectedThisRound() : approved);
        double fillRate = slot.quota() == 0 ? 1.0 : Math.round(1000.0 * filled / slot.quota()) / 1000.0;
        return new PartRow(part, slot.quota(), slot.existingMembers(), slot.selectedThisRound(), bot,
            submitted.size() - bot, approved, count(submitted, ProjectApplicationStatus.REJECTED),
            count(submitted, ProjectApplicationStatus.SUBMITTED), fillRate);
    }

    private static List<PartHeadcount> headcounts(RoundSnapshot snapshot, Set<ChallengerPart> parts) {
        Set<Long> applicants = snapshot.applications().stream().filter(ApplicationRow::submitted)
            .map(ApplicationRow::applicantMemberId).collect(Collectors.toSet());
        return parts.stream().sorted().map(part -> {
            List<Candidate> eligible = snapshot.candidates().stream().filter(c -> c.part() == part).toList();
            int withTeam = (int) eligible.stream().filter(Candidate::hasTeam).count();
            int applied = (int) eligible.stream().filter(c -> applicants.contains(c.memberId())).count();
            int notApplied = (int) eligible.stream()
                .filter(c -> !c.hasTeam() && !applicants.contains(c.memberId())).count();
            return new PartHeadcount(part, eligible.size(), withTeam, eligible.size() - withTeam, applied, notApplied);
        }).toList();
    }

    private static List<ReservedRow> reservedRows(RoundSnapshot snapshot, ChapterRoles roles) {
        Map<String, Candidate> candidates = new HashMap<>();
        snapshot.candidates().forEach(candidate -> candidates.put(candidate.email(), candidate));
        Map<String, ApplicationRow> applications = new HashMap<>();
        snapshot.applications().stream()
            .sorted(Comparator.comparing((ApplicationRow application) -> application.submitted())
                .thenComparing(ApplicationRow::applicationId))
            .forEach(application -> applications.put(application.applicantEmail(), application));
        Set<ChallengerPart> parts = snapshot.round().parts();
        List<ReservedRow> rows = new ArrayList<>();
        for (ReservedSeat seat : roles.reserved()) {
            if (!parts.contains(seat.part())) {
                continue;
            }
            Candidate candidate = candidates.get(seat.email());
            ApplicationRow application = applications.get(seat.email());
            rows.add(new ReservedRow(seat.email(), seat.label(), seat.part(), candidate != null && candidate.hasTeam(),
                application == null ? null : application.status(),
                application == null ? null : application.projectId()));
        }
        return rows;
    }

    private static int count(List<ApplicationRow> applications, ProjectApplicationStatus status) {
        return (int) applications.stream().filter(application -> application.status() == status).count();
    }
}
