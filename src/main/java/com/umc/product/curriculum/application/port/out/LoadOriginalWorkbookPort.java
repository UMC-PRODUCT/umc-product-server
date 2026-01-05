package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.OriginalWorkbook;
import java.util.List;
import java.util.Optional;

public interface LoadOriginalWorkbookPort {

    Optional<OriginalWorkbook> findById(Long id);

    List<OriginalWorkbook> findByCurriculumId(Long curriculumId);

    List<OriginalWorkbook> findByCurriculumIdOrderByOrderNo(Long curriculumId);
}
