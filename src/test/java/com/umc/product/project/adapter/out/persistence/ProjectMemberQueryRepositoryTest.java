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

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import(ProjectMemberQueryRepository.class)
class ProjectMemberQueryRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProjectMemberQueryRepository sut;

    private Project project;
    private ProjectApplicationForm form;
    private ProjectMatchingRound round;

    @BeforeEach
    void setUp() {
        project = persistProject();
        form = em.persist(ProjectApplicationForm.create(project, 500L));
        round = persistRound();
    }

    @Test
    @DisplayName("listApplicationIdsWithActiveMemberByApplicationIds_ACTIVE_멤버와_연결된_지원서만_반환")
    void listApplicationIdsWithActiveMemberOnly() {
        // given
        ProjectApplication activeMemberApplication = persistApplication(200L);
        ProjectApplication dismissedMemberApplication = persistApplication(201L);
        ProjectApplication noMemberApplication = persistApplication(202L);

        em.persist(ProjectMember.createFromApplication(activeMemberApplication, ChallengerPart.WEB, 900L));
        ProjectMember dismissedMember =
            ProjectMember.createFromApplication(dismissedMemberApplication, ChallengerPart.WEB, 900L);
        dismissedMember.dismiss("테스트 퇴출", 900L);
        em.persist(dismissedMember);
        em.persist(ProjectMember.create(project, 203L, ChallengerPart.WEB, 900L));
        em.flush();
        em.clear();

        // when
        List<Long> result = sut.listApplicationIdsWithActiveMemberByApplicationIds(Set.of(
            activeMemberApplication.getId(),
            dismissedMemberApplication.getId(),
            noMemberApplication.getId()
        ));

        // then
        assertThat(result).containsExactly(activeMemberApplication.getId());
    }

    private Project persistProject() {
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
        ReflectionTestUtils.setField(project, "name", "프로젝트 알파");
        ReflectionTestUtils.setField(project, "productOwnerMemberId", 10L);
        ReflectionTestUtils.setField(project, "productOwnerSchoolId", 1L);
        ReflectionTestUtils.setField(project, "creatorMemberId", 10L);
        em.persist(project);
        return project;
    }

    private ProjectMatchingRound persistRound() {
        Instant startsAt = Instant.parse("2026-05-01T00:00:00Z");
        ProjectMatchingRound round = ProjectMatchingRound.create(
            "1차",
            null,
            MatchingType.PLAN_DEVELOPER,
            MatchingPhase.FIRST,
            1L,
            startsAt,
            startsAt.plusSeconds(3_600),
            startsAt.plusSeconds(7_200)
        );
        em.persist(round);
        return round;
    }

    private ProjectApplication persistApplication(Long applicantMemberId) {
        ProjectApplication application = ProjectApplication.create(
            form, applicantMemberId * 10, applicantMemberId, round);
        ReflectionTestUtils.setField(application, "status", ProjectApplicationStatus.APPROVED);
        em.persist(application);
        return application;
    }
}
