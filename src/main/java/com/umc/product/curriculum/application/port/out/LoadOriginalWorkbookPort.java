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

    /**
     * 활성 기수에서 배포된 주차 번호 목록 조회 (드롭다운 필터용)
     *
     * @param part 파트 (null이면 모든 파트)
     * @return 배포된 주차 번호 목록 (오름차순)
     */
    List<Integer> findReleasedWeekNos(ChallengerPart part);
}
