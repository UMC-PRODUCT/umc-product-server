package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.WeeklyCurriculum;
import java.util.List;
import java.util.Optional;

public interface LoadWeeklyCurriculumPort {

    Optional<WeeklyCurriculum> findById(Long id);

    WeeklyCurriculum getById(Long id);

    /**
     * 커리큘럼 하위 주차별 커리큘럼 목록 조회 (주차 오름차순, 부록 마지막)
     *
     * @param weekNo null이면 전체 주차, 값이 있으면 해당 주차만 반환
     */
    List<WeeklyCurriculum> findByCurriculumId(Long curriculumId, Long weekNo);

    boolean existsByCurriculumId(Long curriculumId);

    boolean existsOriginalWorkbookByWeeklyCurriculumId(Long weeklyCurriculumId);

    boolean existsReleasedOriginalWorkbookByWeeklyCurriculumId(Long weeklyCurriculumId);

    boolean existsByCurriculumIdAndWeekNoAndIsExtra(Long curriculumId, Long weekNo, boolean isExtra);

    boolean existsByCurriculumIdAndWeekNoAndIsExtraAndIdNot(Long curriculumId, Long weekNo, boolean isExtra, Long id);
}