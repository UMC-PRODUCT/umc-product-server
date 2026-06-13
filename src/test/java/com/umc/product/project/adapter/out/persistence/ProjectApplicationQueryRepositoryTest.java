package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.project.application.port.out.dto.ProjectMemberMatchedRoundInfo;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import({ProjectApplicationQueryRepository.class})
class ProjectApplicationQueryRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProjectApplicationQueryRepository sut;

    private Project project;
    private ProjectApplicationForm form;

    @BeforeEach
    void setUp() {
        project = persistProject("프로젝트 알파", 10L);
        form = ProjectApplicationForm.create(project, 500L);
        em.persist(form);
    }

    @Test
    @DisplayName("프로젝트_멤버별_APPROVED_지원서_중_최신_매칭차수만_반환한다")
    void listLatestApprovedMatchedRoundsByProjectIdsAndMemberIdsReturnsLatestApprovedOnly() {
        Long memberId = 200L;
        ProjectMatchingRound oldApprovedRound = persistRound(
            "1차",
            MatchingType.PLAN_DEVELOPER,
            MatchingPhase.FIRST,
            Instant.parse("2026-05-01T00:00:00Z")
        );
        ProjectMatchingRound latestApprovedRound = persistRound(
            "2차",
            MatchingType.PLAN_DEVELOPER,
            MatchingPhase.SECOND,
            Instant.parse("2026-05-03T00:00:00Z")
        );
        ProjectMatchingRound tieApprovedRound = persistRound(
            "디자인 1차",
            MatchingType.PLAN_DESIGN,
            MatchingPhase.FIRST,
            Instant.parse("2026-05-03T00:00:00Z")
        );
        ProjectMatchingRound rejectedRound = persistRound(
            "3차",
            MatchingType.PLAN_DEVELOPER,
            MatchingPhase.THIRD,
            Instant.parse("2026-05-05T00:00:00Z")
        );
        ProjectMatchingRound submittedRound = persistRound(
            "디자인 2차",
            MatchingType.PLAN_DESIGN,
            MatchingPhase.SECOND,
            Instant.parse("2026-05-06T00:00:00Z")
        );

        persistApplication(memberId, oldApprovedRound, ProjectApplicationStatus.APPROVED);
        ProjectApplication latestApproved = persistApplication(memberId, latestApprovedRound,
            ProjectApplicationStatus.APPROVED);
        ProjectApplication tieApproved = persistApplication(memberId, tieApprovedRound,
            ProjectApplicationStatus.APPROVED);
        persistApplication(memberId, rejectedRound, ProjectApplicationStatus.REJECTED);
        persistApplication(memberId, submittedRound, ProjectApplicationStatus.SUBMITTED);
        persistApplication(201L, rejectedRound, ProjectApplicationStatus.REJECTED);
        em.flush();
        em.clear();

        List<ProjectMemberMatchedRoundInfo> result = sut.listLatestApprovedMatchedRoundsByProjectIdsAndMemberIds(
            Set.of(project.getId()),
            Set.of(memberId, 201L)
        );

        assertThat(result).containsExactly(new ProjectMemberMatchedRoundInfo(
            project.getId(),
            memberId,
            tieApprovedRound.getId(),
            MatchingType.PLAN_DESIGN,
            MatchingPhase.FIRST
        ));
        assertThat(tieApproved.getId()).isGreaterThan(latestApproved.getId());
    }

    private Project persistProject(String name, Long ownerId) {
        Project project;
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            project = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(project, "gisuId", 1L);
        ReflectionTestUtils.setField(project, "chapterId", 1L);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(project, "name", name);
        ReflectionTestUtils.setField(project, "productOwnerMemberId", ownerId);
        ReflectionTestUtils.setField(project, "productOwnerSchoolId", 1L);
        ReflectionTestUtils.setField(project, "creatorMemberId", ownerId);
        em.persist(project);
        return project;
    }

    private ProjectMatchingRound persistRound(
        String name,
        MatchingType type,
        MatchingPhase phase,
        Instant startsAt
    ) {
        ProjectMatchingRound round = ProjectMatchingRound.create(
            name,
            null,
            type,
            phase,
            1L,
            startsAt,
            startsAt.plusSeconds(3_600),
            startsAt.plusSeconds(7_200)
        );
        em.persist(round);
        return round;
    }

    private ProjectApplication persistApplication(
        Long applicantMemberId,
        ProjectMatchingRound round,
        ProjectApplicationStatus status
    ) {
        ProjectApplication application = ProjectApplication.create(
            form, applicantMemberId * 10, applicantMemberId, round);
        ReflectionTestUtils.setField(application, "status", status);
        em.persist(application);
        return application;
    }
}
