package com.umc.product.curriculum.application.port.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.CurriculumWeekInfo;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import java.util.List;

public interface LoadOriginalWorkbookPort {

    OriginalWorkbook findById(Long id);

    List<OriginalWorkbook> findByCurriculumId(Long curriculumId);

    List<OriginalWorkbook> findByCurriculumIdOrderByWeekNo(Long curriculumId);

    /**
     * 기수의 모든 주차 번호 조회 (드롭다운용)
     */
    List<Integer> findDistinctWeekNoByGisuId(Long gisuId);

    /**
     * 활성 기수의 파트별 커리큘럼 주차 정보 조회 (weekNo, title만 projection)
     */
    List<CurriculumWeekInfo> findWeekInfoByActiveGisuAndPart(ChallengerPart part);
}
