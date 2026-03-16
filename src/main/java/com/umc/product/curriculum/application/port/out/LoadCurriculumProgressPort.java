package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.application.port.in.query.dto.WorkbookProgressProjection;
import java.util.List;

public interface LoadCurriculumProgressPort {

    List<WorkbookProgressProjection> findWorkbookProgressProjections(Long curriculumId, Long challengerId);
}
