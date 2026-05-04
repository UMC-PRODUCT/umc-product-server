package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.TestContainersConfig;
import java.util.Optional;
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
    ProjectApplicationFormPersistenceAdapter.class})
class ProjectApplicationFormPersistenceAdapterTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProjectApplicationFormPersistenceAdapter sut;

    private Long projectId;
    private Long otherProjectId;

    @BeforeEach
    void setUp() {
        projectId = persistProject("프로젝트 알파", 10L).getId();
        otherProjectId = persistProject("프로젝트 베타", 11L).getId();
        em.flush();
        em.clear();
    }

    @Test
    void existsByProjectId_폼이_없으면_false() {
        assertThat(sut.existsByProjectId(projectId)).isFalse();
    }

    @Test
    void existsByProjectId_폼이_하나라도_있으면_true() {
        Project project = em.find(Project.class, projectId);
        em.persist(ProjectApplicationForm.create(project, 999L));
        em.flush();
        em.clear();

        assertThat(sut.existsByProjectId(projectId)).isTrue();
        assertThat(sut.existsByProjectId(otherProjectId)).isFalse();
    }

    @Test
    void findByProjectId_폼이_없으면_empty() {
        assertThat(sut.findByProjectId(projectId)).isEmpty();
    }

    @Test
    void findByProjectId_여러_폼_중_가장_먼저_생성된_것을_반환() {
        Project project = em.find(Project.class, projectId);
        Long firstId = em.persistAndGetId(ProjectApplicationForm.create(project, 100L), Long.class);
        em.persist(ProjectApplicationForm.create(project, 200L));
        em.flush();
        em.clear();

        Optional<ProjectApplicationForm> found = sut.findByProjectId(projectId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(firstId);
        assertThat(found.get().getFormId()).isEqualTo(100L);
    }

    @Test
    void save_새_폼이_영속화된다() {
        Project project = em.find(Project.class, projectId);

        ProjectApplicationForm saved = sut.save(ProjectApplicationForm.create(project, 500L));
        em.flush();
        em.clear();

        assertThat(saved.getId()).isNotNull();
        ProjectApplicationForm reloaded = em.find(ProjectApplicationForm.class, saved.getId());
        assertThat(reloaded.getFormId()).isEqualTo(500L);
        assertThat(reloaded.getProject().getId()).isEqualTo(projectId);
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
        ReflectionTestUtils.setField(project, "status", ProjectStatus.DRAFT);
        ReflectionTestUtils.setField(project, "name", name);
        ReflectionTestUtils.setField(project, "productOwnerMemberId", ownerId);
        ReflectionTestUtils.setField(project, "productOwnerSchoolId", 1L);
        em.persist(project);
        return project;
    }
}
