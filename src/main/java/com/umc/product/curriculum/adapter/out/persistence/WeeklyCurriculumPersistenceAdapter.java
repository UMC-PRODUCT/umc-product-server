package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.application.port.out.LoadWeeklyCurriculumPort;
import com.umc.product.curriculum.application.port.out.SaveWeeklyCurriculumPort;
import com.umc.product.curriculum.domain.WeeklyCurriculum;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WeeklyCurriculumPersistenceAdapter implements LoadWeeklyCurriculumPort, SaveWeeklyCurriculumPort {

    private final WeeklyCurriculumJpaRepository weeklyCurriculumJpaRepository;
    private final WeeklyCurriculumQueryRepository weeklyCurriculumQueryRepository;

    @Override
    public Optional<WeeklyCurriculum> findById(Long id) {
        return weeklyCurriculumJpaRepository.findById(id);
    }

    @Override
    public WeeklyCurriculum getById(Long id) {
        return weeklyCurriculumJpaRepository.findById(id)
            .orElseThrow(() -> new CurriculumDomainException(CurriculumErrorCode.WEEKLY_CURRICULUM_NOT_FOUND));
    }

    @Override
    public List<WeeklyCurriculum> findByCurriculumId(Long curriculumId, Long weekNo) {
        if (weekNo != null) {
            return weeklyCurriculumJpaRepository.findByCurriculumIdAndWeekNoOrderByIsExtraAsc(curriculumId, weekNo);
        }
        return weeklyCurriculumJpaRepository.findByCurriculumIdOrderByWeekNoAscIsExtraAsc(curriculumId);
    }

    @Override
    public boolean existsByCurriculumId(Long curriculumId) {
        return weeklyCurriculumJpaRepository.existsByCurriculumId(curriculumId);
    }

    @Override
    public boolean existsOriginalWorkbookByWeeklyCurriculumId(Long weeklyCurriculumId) {
        return weeklyCurriculumQueryRepository.existsOriginalWorkbook(weeklyCurriculumId, null);
    }

    @Override
    public boolean existsReleasedOriginalWorkbookByWeeklyCurriculumId(Long weeklyCurriculumId) {
        return weeklyCurriculumQueryRepository.existsOriginalWorkbook(weeklyCurriculumId, OriginalWorkbookStatus.RELEASED);
    }

    @Override
    public boolean existsByCurriculumIdAndWeekNoAndIsExtra(Long curriculumId, Long weekNo, boolean isExtra) {
        return weeklyCurriculumJpaRepository.existsByCurriculumIdAndWeekNoAndIsExtra(curriculumId, weekNo, isExtra);
    }

    @Override
    public boolean existsByCurriculumIdAndWeekNoAndIsExtraAndIdNot(Long curriculumId, Long weekNo, boolean isExtra, Long id) {
        return weeklyCurriculumJpaRepository.existsByCurriculumIdAndWeekNoAndIsExtraAndIdNot(curriculumId, weekNo, isExtra, id);
    }

    @Override
    public WeeklyCurriculum save(WeeklyCurriculum weeklyCurriculum) {
        return weeklyCurriculumJpaRepository.save(weeklyCurriculum);
    }

    @Override
    public void delete(WeeklyCurriculum weeklyCurriculum) {
        weeklyCurriculumJpaRepository.delete(weeklyCurriculum);
    }
}
