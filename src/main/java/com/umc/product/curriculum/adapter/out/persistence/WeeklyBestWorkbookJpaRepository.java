package com.umc.product.curriculum.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.curriculum.domain.WeeklyBestWorkbook;

public interface WeeklyBestWorkbookJpaRepository extends JpaRepository<WeeklyBestWorkbook, Long> {

    boolean existsByWeeklyCurriculum_IdAndStudyGroupId(Long weeklyCurriculumId, Long studyGroupId);

    List<WeeklyBestWorkbook> findByWeeklyCurriculum_IdAndStudyGroupId(Long weeklyCurriculumId, Long studyGroupId);
}
