package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OriginalWorkbookJpaRepository extends JpaRepository<OriginalWorkbook, Long> {

    List<OriginalWorkbook> findByWeeklyCurriculumIdInAndOriginalWorkbookStatus(
        List<Long> weeklyCurriculumIds, OriginalWorkbookStatus originalWorkbookStatus);

//    List<OriginalWorkbook> findByCurriculumId(Long curriculumId);
//
//    List<OriginalWorkbook> findByCurriculumIdOrderByWeekNoAsc(Long curriculumId);
//
//    List<OriginalWorkbook> findByCurriculumIdIn(List<Long> curriculumIds);
//
//    @Query("SELECT DISTINCT o.weekNo FROM OriginalWorkbook o " +
//        "WHERE o.curriculum.gisuId = :gisuId ORDER BY o.weekNo")
//    List<Integer> findDistinctWeekNoByGisuId(@Param("gisuId") Long gisuId);
}
