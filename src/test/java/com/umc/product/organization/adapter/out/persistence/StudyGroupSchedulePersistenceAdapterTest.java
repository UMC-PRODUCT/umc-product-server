package com.umc.product.organization.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.organization.domain.StudyGroupSchedule;
import com.umc.product.support.TestContainersConfig;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    JpaConfig.class,
    QueryDslConfig.class,
    TestContainersConfig.class,
    StudyGroupSchedulePersistenceAdapter.class
})
class StudyGroupSchedulePersistenceAdapterTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    StudyGroupSchedulePersistenceAdapter sut;

    @Test
    void findScheduleIdsByStudyGroupIds_매핑된_scheduleIds_반환() {
        // given — group1 → schedule 100, 101 / group2 → schedule 200 / group3 → schedule 999 (조회 대상 제외)
        Long group1 = 1L;
        Long group2 = 2L;
        Long group3 = 3L;

        persistMapping(group1, 100L, 10L);
        persistMapping(group1, 101L, 11L);
        persistMapping(group2, 200L, 20L);
        persistMapping(group3, 999L, 30L);
        em.flush();
        em.clear();

        // when — group1, group2 만 조회
        Set<Long> result = sut.findScheduleIdsByStudyGroupIds(List.of(group1, group2));

        // then
        assertThat(result)
            .containsExactlyInAnyOrder(100L, 101L, 200L)
            .doesNotContain(999L);
    }

    @Test
    void findScheduleIdsByStudyGroupIds_매핑_없으면_빈_Set() {
        // when
        Set<Long> result = sut.findScheduleIdsByStudyGroupIds(List.of(99999L));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findScheduleIdsByStudyGroupIds_빈_입력은_DB_호출없이_빈_Set() {
        // when
        Set<Long> result = sut.findScheduleIdsByStudyGroupIds(List.of());

        // then
        assertThat(result).isEmpty();
    }

    // ========== Helper Methods ==========

    private void persistMapping(Long studyGroupId, Long scheduleId, Long weeklyCurriculumId) {
        StudyGroupSchedule entity = StudyGroupSchedule.builder()
            .studyGroupId(studyGroupId)
            .scheduleId(scheduleId)
            .weeklyCurriculumId(weeklyCurriculumId)
            .build();
        em.persist(entity);
    }
}
