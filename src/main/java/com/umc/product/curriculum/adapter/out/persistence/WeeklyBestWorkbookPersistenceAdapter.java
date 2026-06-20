package com.umc.product.curriculum.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.umc.product.curriculum.application.port.out.LoadWeeklyBestWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveWeeklyBestWorkbookPort;
import com.umc.product.curriculum.domain.WeeklyBestWorkbook;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WeeklyBestWorkbookPersistenceAdapter implements LoadWeeklyBestWorkbookPort, SaveWeeklyBestWorkbookPort {

    private final WeeklyBestWorkbookJpaRepository weeklyBestWorkbookJpaRepository;

    @Override
    public WeeklyBestWorkbook getById(Long id) {
        return weeklyBestWorkbookJpaRepository.findById(id)
            .orElseThrow(() -> new CurriculumDomainException(
                CurriculumErrorCode.WORKBOOK_NOT_FOUND,
                "베스트 워크북을 찾을 수 없어요. 선택한 베스트 워크북을 확인해주세요."
            ));
    }

    @Override
    public boolean existsByWeeklyCurriculumIdAndStudyGroupId(Long weeklyCurriculumId, Long studyGroupId) {
        return weeklyBestWorkbookJpaRepository.existsByWeeklyCurriculum_IdAndStudyGroupId(
            weeklyCurriculumId,
            studyGroupId
        );
    }

    @Override
    public List<WeeklyBestWorkbook> listByWeeklyCurriculumIdAndStudyGroupId(Long weeklyCurriculumId, Long studyGroupId) {
        return weeklyBestWorkbookJpaRepository.findByWeeklyCurriculum_IdAndStudyGroupId(
            weeklyCurriculumId,
            studyGroupId
        );
    }

    @Override
    public WeeklyBestWorkbook save(WeeklyBestWorkbook weeklyBestWorkbook) {
        return weeklyBestWorkbookJpaRepository.save(weeklyBestWorkbook);
    }

    @Override
    public void delete(WeeklyBestWorkbook weeklyBestWorkbook) {
        weeklyBestWorkbookJpaRepository.delete(weeklyBestWorkbook);
    }
}
