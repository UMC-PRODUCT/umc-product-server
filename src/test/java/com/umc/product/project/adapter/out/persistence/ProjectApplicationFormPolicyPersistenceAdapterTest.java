package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import com.umc.product.project.domain.enums.FormSectionType;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.TestContainersConfig;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QueryDslConfig.class, TestContainersConfig.class,
    ProjectApplicationFormPolicyPersistenceAdapter.class})
class ProjectApplicationFormPolicyPersistenceAdapterTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProjectApplicationFormPolicyPersistenceAdapter sut;

    private ProjectApplicationForm form;
    private ProjectApplicationForm otherForm;

    @BeforeEach
    void setUp() {
        Project project = persistProject("프로젝트 알파", 10L);
        Project otherProject = persistProject("프로젝트 베타", 11L);
        form = em.persist(ProjectApplicationForm.create(project, 100L));
        otherForm = em.persist(ProjectApplicationForm.create(otherProject, 200L));
        em.flush();
    }

    @Test
    void save_COMMON_정책_저장_후_조회() {
        ProjectApplicationFormPolicy saved = sut.save(
            ProjectApplicationFormPolicy.createCommon(form, 500L)
        );
        em.flush();
        em.clear();

        ProjectApplicationFormPolicy reloaded =
            em.find(ProjectApplicationFormPolicy.class, saved.getId());
        assertThat(reloaded.getType()).isEqualTo(FormSectionType.COMMON);
        assertThat(reloaded.getAllowedParts()).isEmpty();
        assertThat(reloaded.getFormSectionId()).isEqualTo(500L);
    }

    @Test
    void save_PART_정책_저장_후_조회() {
        ProjectApplicationFormPolicy saved = sut.save(
            ProjectApplicationFormPolicy.createForParts(
                form, 501L, Set.of(ChallengerPart.WEB, ChallengerPart.IOS)
            )
        );
        em.flush();
        em.clear();

        ProjectApplicationFormPolicy reloaded =
            em.find(ProjectApplicationFormPolicy.class, saved.getId());
        assertThat(reloaded.getType()).isEqualTo(FormSectionType.PART);
        assertThat(reloaded.getAllowedParts())
            .containsExactlyInAnyOrder(ChallengerPart.WEB, ChallengerPart.IOS);
    }

    @Test
    void listByApplicationFormId_해당_폼의_정책만_반환() {
        sut.save(ProjectApplicationFormPolicy.createCommon(form, 510L));
        sut.save(ProjectApplicationFormPolicy.createForParts(form, 511L, Set.of(ChallengerPart.DESIGN)));
        sut.save(ProjectApplicationFormPolicy.createCommon(otherForm, 520L));
        em.flush();
        em.clear();

        List<ProjectApplicationFormPolicy> result = sut.listByApplicationFormId(form.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(ProjectApplicationFormPolicy::getFormSectionId)
            .containsExactlyInAnyOrder(510L, 511L);
    }

    @Test
    void deleteByFormSectionId_매칭되는_row_제거() {
        sut.save(ProjectApplicationFormPolicy.createCommon(form, 530L));
        sut.save(ProjectApplicationFormPolicy.createCommon(form, 531L));
        em.flush();
        em.clear();

        sut.deleteByFormSectionId(530L);
        em.flush();
        em.clear();

        List<ProjectApplicationFormPolicy> remaining = sut.listByApplicationFormId(form.getId());
        assertThat(remaining)
            .extracting(ProjectApplicationFormPolicy::getFormSectionId)
            .containsExactly(531L);
    }

    // CHECK 제약(`chk_policy_part_consistency`, `chk_policy_type_value`) 검증은
    // Flyway 마이그레이션이 적용된 환경에서만 가능하다. 본 테스트는 ddl-auto=create-drop 으로
    // 스키마를 생성하므로 CHECK 가 만들어지지 않아 직접 검증할 수 없다. 마이그레이션 SQL 자체는
    // dev/staging 의 실 PostgreSQL 에서 1차 검증되며, 향후 Flyway 기반 통합 테스트가 추가되면
    // 그곳에서 다시 검증한다.

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
        ReflectionTestUtils.setField(project, "status", ProjectStatus.DRAFT);
        ReflectionTestUtils.setField(project, "name", name);
        ReflectionTestUtils.setField(project, "productOwnerMemberId", ownerId);
        em.persist(project);
        return project;
    }
}
