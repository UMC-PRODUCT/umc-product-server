package com.umc.product.curriculum.application.port.out;

import java.util.List;

import com.umc.product.curriculum.domain.WeeklyBestWorkbook;

public interface LoadWeeklyBestWorkbookPort {

    WeeklyBestWorkbook getById(Long id);

    boolean existsByWeeklyCurriculumIdAndStudyGroupId(Long weeklyCurriculumId, Long studyGroupId);

    List<WeeklyBestWorkbook> listByWeeklyCurriculumIdAndStudyGroupId(Long weeklyCurriculumId, Long studyGroupId);
}
