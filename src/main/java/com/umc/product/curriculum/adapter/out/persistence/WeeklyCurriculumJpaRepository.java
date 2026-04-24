package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.WeeklyCurriculum;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WeeklyCurriculumJpaRepository extends JpaRepository<WeeklyCurriculum, Long> {

    boolean existsByCurriculumId(Long curriculumId);

    List<WeeklyCurriculum> findByCurriculumIdOrderByWeekNoAscIsExtraAsc(Long curriculumId);

    List<WeeklyCurriculum> findByCurriculumIdAndWeekNoOrderByIsExtraAsc(Long curriculumId, Long weekNo);

    @Query("SELECT COUNT(o) > 0 FROM OriginalWorkbook o WHERE o.weeklyCurriculum.id = :weeklyCurriculumId")
    boolean existsOriginalWorkbookByWeeklyCurriculumId(@Param("weeklyCurriculumId") Long weeklyCurriculumId);

    boolean existsByCurriculumIdAndWeekNoAndIsExtra(Long curriculumId, Long weekNo, boolean isExtra);

    boolean existsByCurriculumIdAndWeekNoAndIsExtraAndIdNot(Long curriculumId, Long weekNo, boolean isExtra, Long id);

    @Query("SELECT COUNT(o) > 0 FROM OriginalWorkbook o " +
        "WHERE o.weeklyCurriculum.id = :weeklyCurriculumId " +
        "AND o.originalWorkbookStatus = :originalWorkbookStatus")
    boolean existsOriginalWorkbookByWeeklyCurriculumIdAndStatus(
        @Param("weeklyCurriculumId") Long weeklyCurriculumId,
        @Param("originalWorkbookStatus") OriginalWorkbookStatus originalWorkbookStatus);
}
