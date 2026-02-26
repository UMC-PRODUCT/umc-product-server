package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.OriginalWorkbook;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OriginalWorkbookJpaRepository extends JpaRepository<OriginalWorkbook, Long> {

    List<OriginalWorkbook> findByCurriculumId(Long curriculumId);

    List<OriginalWorkbook> findByCurriculumIdOrderByWeekNoAsc(Long curriculumId);

    @Query("SELECT DISTINCT o.weekNo FROM OriginalWorkbook o " +
            "WHERE o.curriculum.gisuId = :gisuId ORDER BY o.weekNo")
    List<Integer> findDistinctWeekNoByGisuId(@Param("gisuId") Long gisuId);
}
