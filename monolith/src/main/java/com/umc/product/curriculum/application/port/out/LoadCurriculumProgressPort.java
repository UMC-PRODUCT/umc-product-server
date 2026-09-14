package com.umc.product.curriculum.application.port.out;

import java.util.List;

import com.umc.product.curriculum.application.port.in.query.dto.WorkbookProgressProjection;

public interface LoadCurriculumProgressPort {

    List<WorkbookProgressProjection> findWorkbookProgressProjections(Long curriculumId, Long challengerId);
}
