package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.TestContainersConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QueryDslConfig.class, TestContainersConfig.class, ProjectQueryRepository.class})
class ProjectQueryRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProjectQueryRepository sut;

    private Long gisuId;

    @BeforeEach
    void setUp() {
        gisuId = 1L;

        persistProject("프로젝트 알파", ProjectStatus.IN_PROGRESS, gisuId, 1L, 10L);
        persistProject("프로젝트 베타", ProjectStatus.IN_PROGRESS, gisuId, 1L, 11L);
        persistProject("프로젝트 감마", ProjectStatus.DRAFT, gisuId, 1L, 12L);
        persistProject("프로젝트 델타", ProjectStatus.IN_PROGRESS, gisuId, 2L, 13L);
        persistProject("다른기수 프로젝트", ProjectStatus.IN_PROGRESS, 2L, 1L, 14L);

        em.flush();
        em.clear();
    }

    @Test
    void 기수_필터로_조회() {
        // given
        SearchProjectQuery query = SearchProjectQuery.forChallenger(
            gisuId, null, null, null, null, null, PageRequest.of(0, 20));

        // when
        Page<Project> result = sut.search(query);

        // then — gisuId=1 + IN_PROGRESS만
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
            .allMatch(p -> p.getGisuId().equals(gisuId))
            .allMatch(p -> p.getStatus() == ProjectStatus.IN_PROGRESS);
    }

    @Test
    void 이름_키워드_검색() {
        // given
        SearchProjectQuery query = SearchProjectQuery.forChallenger(
            gisuId, "알파", null, null, null, null, PageRequest.of(0, 20));

        // when
        Page<Project> result = sut.search(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("프로젝트 알파");
    }

    @Test
    void 지부_필터() {
        // given
        SearchProjectQuery query = SearchProjectQuery.forChallenger(
            gisuId, null, 2L, null, null, null, PageRequest.of(0, 20));

        // when
        Page<Project> result = sut.search(query);

        // then — chapterId=2인 프로젝트만
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("프로젝트 델타");
    }

    @Test
    void 상태_필터_Admin용() {
        // given — DRAFT 포함 조회
        SearchProjectQuery query = SearchProjectQuery.forAdmin(
            gisuId, null, null, null, null, null,
            List.of(ProjectStatus.DRAFT), PageRequest.of(0, 20));

        // when
        Page<Project> result = sut.search(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(ProjectStatus.DRAFT);
    }

    @Test
    void 조합_필터_키워드와_지부() {
        // given
        SearchProjectQuery query = SearchProjectQuery.forChallenger(
            gisuId, "프로젝트", 1L, null, null, null, PageRequest.of(0, 20));

        // when
        Page<Project> result = sut.search(query);

        // then — chapterId=1 + name contains "프로젝트" + IN_PROGRESS
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void 페이지네이션() {
        // given
        SearchProjectQuery query = SearchProjectQuery.forChallenger(
            gisuId, null, null, null, null, null, PageRequest.of(0, 2));

        // when
        Page<Project> result = sut.search(query);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void 정렬_createdAt_DESC() {
        // given
        SearchProjectQuery query = SearchProjectQuery.forChallenger(
            gisuId, null, null, null, null, null, PageRequest.of(0, 20));

        // when
        Page<Project> result = sut.search(query);

        // then — createdAt 내림차순
        List<Project> content = result.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            assertThat(content.get(i).getCreatedAt())
                .isAfterOrEqualTo(content.get(i + 1).getCreatedAt());
        }
    }

    @Test
    void 빈_결과() {
        // given
        SearchProjectQuery query = SearchProjectQuery.forChallenger(
            999L, null, null, null, null, null, PageRequest.of(0, 20));

        // when
        Page<Project> result = sut.search(query);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // ========== Helper ==========

    private void persistProject(String name, ProjectStatus status, Long gisuId, Long chapterId, Long ownerId) {
        Project project;
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            project = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(project, "gisuId", gisuId);
        ReflectionTestUtils.setField(project, "chapterId", chapterId);
        ReflectionTestUtils.setField(project, "status", status);
        ReflectionTestUtils.setField(project, "name", name);
        ReflectionTestUtils.setField(project, "productOwnerMemberId", ownerId);
        em.persist(project);
    }
}
