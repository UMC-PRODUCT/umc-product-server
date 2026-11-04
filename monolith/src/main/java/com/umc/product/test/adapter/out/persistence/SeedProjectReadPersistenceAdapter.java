package com.umc.product.test.adapter.out.persistence;

import static com.umc.product.test.adapter.out.persistence.MatchingRoundRows.toInt;
import static com.umc.product.test.adapter.out.persistence.MatchingRoundRows.toLong;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.test.application.port.out.SeedProjectReadPort;
import com.umc.product.test.domain.rehearsal.RoundSnapshot;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ApplicationRow;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.Candidate;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.PartSlot;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.ProjectSlot;
import com.umc.product.test.domain.rehearsal.RoundSnapshot.Round;
import com.umc.product.test.domain.startingline.OwnedProject;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SeedProjectReadPersistenceAdapter implements SeedProjectReadPort {

    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<OwnedProject> listOwnedProjects(Long gisuId, Collection<Long> ownerMemberIds) {
        if (ownerMemberIds.isEmpty()) {
            return List.of();
        }
        List<Object[]> projects = rows("""
            SELECT DISTINCT ON (p.product_owner_member_id)
                   p.id, p.product_owner_member_id, p.status,
                   COALESCE(p.name, '') <> '',
                   EXISTS (SELECT 1 FROM project_application_form paf WHERE paf.project_id = p.id)
            FROM project p
            WHERE p.gisu_id = :gisuId
              AND p.product_owner_member_id IN (:ownerIds)
              AND p.status IN ('DRAFT', 'PENDING_REVIEW', 'IN_PROGRESS')
            ORDER BY p.product_owner_member_id, p.id
            """, Map.of("gisuId", gisuId, "ownerIds", ownerMemberIds));
        if (projects.isEmpty()) {
            return List.of();
        }
        Map<Long, Map<ChallengerPart, Integer>> quotas = quotasByProject(
            projects.stream().map(row -> toLong(row[0])).toList());
        List<OwnedProject> result = new ArrayList<>();
        for (Object[] row : projects) {
            Long projectId = toLong(row[0]);
            result.add(new OwnedProject(projectId, toLong(row[1]), ProjectStatus.valueOf((String) row[2]),
                (Boolean) row[3], (Boolean) row[4], quotas.getOrDefault(projectId, Map.of())));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoundSnapshot> findRoundSnapshot(Long roundId) {
        Instant observedAt = Instant.now();
        List<Object[]> roundRows = rows("""
            SELECT r.name, r.chapter_id, c.name, c.gisu_id, r.type, %s
            FROM project_matching_round r
            JOIN chapter c ON c.id = r.chapter_id
            WHERE r.id = :roundId
            """.formatted(MatchingRoundRows.TIME_COLUMNS), Map.of("roundId", roundId));
        if (roundRows.isEmpty()) {
            return Optional.empty();
        }
        Object[] row = roundRows.getFirst();
        Long gisuId = toLong(row[3]);
        Round round = new Round((String) row[0], toLong(row[1]), (String) row[2],
            MatchingType.valueOf((String) row[4]), MatchingRoundRows.roundTimes(row, 5));
        return Optional.of(new RoundSnapshot(round, observedAt, candidates(round, gisuId), projects(round, gisuId),
            applications(round, gisuId), rejectedLastRound(round)));
    }

    private List<Candidate> candidates(Round round, Long gisuId) {
        return rows("""
            SELECT m.id, m.email, ch.part,
                   EXISTS (SELECT 1 FROM project_member pm JOIN project p ON p.id = pm.project_id
                           WHERE pm.member_id = m.id AND p.gisu_id = :gisuId
                             AND pm.status IN ('ACTIVE', 'PENDING_PUBLICATION'))
            FROM challenger ch
            JOIN member m ON m.id = ch.member_id
            LEFT JOIN project_matching_chapter_transfer t
                   ON t.member_id = ch.member_id AND t.gisu_id = :gisuId AND t.released_at IS NULL
            LEFT JOIN chapter_school cs
                   ON cs.school_id = m.school_id AND cs.chapter_id IN (SELECT id FROM chapter WHERE gisu_id = :gisuId)
            WHERE ch.gisu_id = :gisuId AND ch.status = 'ACTIVE' AND ch.part IN (:parts)
              AND COALESCE(t.to_chapter_id, cs.chapter_id) = :chapterId
            ORDER BY m.id
            """, Map.of("gisuId", gisuId, "chapterId", round.chapterId(), "parts", partNames(round)))
            .stream()
            .map(row -> new Candidate(toLong(row[0]), (String) row[1], ChallengerPart.valueOf((String) row[2]),
                (Boolean) row[3]))
            .toList();
    }

    private List<ProjectSlot> projects(Round round, Long gisuId) {
        List<Object[]> projectRows = rows("""
            SELECT p.id, p.name, p.product_owner_member_id, m.email
            FROM project p
            JOIN member m ON m.id = p.product_owner_member_id
            WHERE p.gisu_id = :gisuId AND p.chapter_id = :chapterId AND p.status = 'IN_PROGRESS'
            ORDER BY p.id
            """, Map.of("gisuId", gisuId, "chapterId", round.chapterId()));
        if (projectRows.isEmpty()) {
            return List.of();
        }
        List<Long> projectIds = projectRows.stream().map(row -> toLong(row[0])).toList();
        Map<Long, Map<ChallengerPart, Integer>> quotas = quotasByProject(projectIds);
        Map<MemberCountKey, Integer> members = new HashMap<>();
        for (Object[] row : rows("""
            SELECT pm.project_id, pm.part, COALESCE(pm.assigned_matching_round_id = :roundId, FALSE), count(*)
            FROM project_member pm
            WHERE pm.project_id IN (:ids) AND pm.status IN ('ACTIVE', 'PENDING_PUBLICATION')
            GROUP BY 1, 2, 3
            """, Map.of("ids", projectIds, "roundId", round.times().roundId()))) {
            members.put(new MemberCountKey(toLong(row[0]), ChallengerPart.valueOf((String) row[1]), (Boolean) row[2]),
                toInt(row[3]));
        }
        List<ProjectSlot> projects = new ArrayList<>();
        for (Object[] row : projectRows) {
            Long projectId = toLong(row[0]);
            Map<ChallengerPart, PartSlot> parts = new EnumMap<>(ChallengerPart.class);
            quotas.getOrDefault(projectId, Map.of()).forEach((part, quota) -> parts.put(part, new PartSlot(quota,
                members.getOrDefault(new MemberCountKey(projectId, part, false), 0),
                members.getOrDefault(new MemberCountKey(projectId, part, true), 0))));
            projects.add(new ProjectSlot(projectId, (String) row[1], toLong(row[2]), (String) row[3], parts));
        }
        return projects;
    }

    private List<ApplicationRow> applications(Round round, Long gisuId) {
        return rows("""
            SELECT pa.id, paf.project_id, pa.applicant_member_id, m.email, ch.part, pa.status,
                   EXISTS (SELECT 1 FROM answer a
                           WHERE a.form_response_id = pa.form_response_id AND a.text_value LIKE :botPrefix)
            FROM project_application pa
            JOIN project_application_form paf ON paf.id = pa.project_application_form_id
            JOIN member m ON m.id = pa.applicant_member_id
            LEFT JOIN challenger ch ON ch.member_id = pa.applicant_member_id AND ch.gisu_id = :gisuId
            WHERE pa.applied_matching_round_id = :roundId AND pa.status <> 'CANCELLED'
            ORDER BY pa.id
            """, Map.of("roundId", round.times().roundId(), "gisuId", gisuId,
            "botPrefix", RoundSnapshot.BOT_ANSWER_PREFIX + "%"))
            .stream()
            .map(row -> new ApplicationRow(toLong(row[0]), toLong(row[1]), toLong(row[2]), (String) row[3],
                row[4] == null ? null : ChallengerPart.valueOf((String) row[4]),
                ProjectApplicationStatus.valueOf((String) row[5]), (Boolean) row[6]))
            .toList();
    }

    private Map<Long, Set<Long>> rejectedLastRound(Round round) {
        Optional<MatchingPhase> previous = switch (round.times().phase()) {
            case SECOND -> Optional.of(MatchingPhase.FIRST);
            case THIRD -> Optional.of(MatchingPhase.SECOND);
            case FIRST, RANDOM_MATCHING -> Optional.empty();
        };
        if (previous.isEmpty()) {
            return Map.of();
        }
        Map<Long, Set<Long>> rejected = new HashMap<>();
        for (Object[] row : rows("""
            SELECT pa.applicant_member_id, paf.project_id
            FROM project_application pa
            JOIN project_application_form paf ON paf.id = pa.project_application_form_id
            JOIN project_matching_round r ON r.id = pa.applied_matching_round_id
            WHERE r.chapter_id = :chapterId AND r.type = :type AND r.phase = :phase AND pa.status = 'REJECTED'
            """, Map.of("chapterId", round.chapterId(), "type", round.type().name(), "phase",
            previous.get().name()))) {
            rejected.computeIfAbsent(toLong(row[0]), id -> new HashSet<>()).add(toLong(row[1]));
        }
        return rejected;
    }

    private static List<String> partNames(Round round) {
        return round.parts().stream().map(ChallengerPart::name).toList();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> rows(String sql, Map<String, Object> parameters) {
        var query = entityManager.createNativeQuery(sql);
        parameters.forEach(query::setParameter);
        return query.getResultList();
    }

    private Map<Long, Map<ChallengerPart, Integer>> quotasByProject(List<Long> projectIds) {
        Map<Long, Map<ChallengerPart, Integer>> quotas = new HashMap<>();
        for (Object[] row : rows("""
            SELECT ppq.project_id, ppq.part, ppq.quota FROM project_part_quota ppq WHERE ppq.project_id IN (:ids)
            """, Map.of("ids", projectIds))) {
            quotas.computeIfAbsent(toLong(row[0]), id -> new EnumMap<>(ChallengerPart.class))
                .put(ChallengerPart.valueOf((String) row[1]), toInt(row[2]));
        }
        return quotas;
    }

    private record MemberCountKey(Long projectId, ChallengerPart part, boolean selectedThisRound) {
    }
}
