package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.WeeklyCurriculum;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyCurriculumJpaRepository extends JpaRepository<WeeklyCurriculum, Long> {

    boolean existsByCurriculumId(Long curriculumId);

    List<WeeklyCurriculum> findByCurriculumIdOrderByWeekNoAscIsExtraAsc(Long curriculumId);

    List<WeeklyCurriculum> findByCurriculumIdAndWeekNoOrderByIsExtraAsc(Long curriculumId, Long weekNo);

    boolean existsByCurriculumIdAndWeekNoAndIsExtra(Long curriculumId, Long weekNo, boolean isExtra);

    boolean existsByCurriculumIdAndWeekNoAndIsExtraAndIdNot(Long curriculumId, Long weekNo, boolean isExtra, Long id);
}
