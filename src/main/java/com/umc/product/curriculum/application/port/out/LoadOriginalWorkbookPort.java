package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.OriginalWorkbook;
import java.util.List;
import java.util.Optional;

public interface LoadOriginalWorkbookPort {

    Optional<OriginalWorkbook> findById(Long id);

    List<OriginalWorkbook> findByCurriculumId(Long curriculumId);

    List<OriginalWorkbook> findByCurriculumIdOrderByWeekNo(Long curriculumId);

    /**
     * 기수의 모든 주차 번호 조회 (드롭다운용)
     */
    List<Integer> findDistinctWeekNoByGisuId(Long gisuId);
}
