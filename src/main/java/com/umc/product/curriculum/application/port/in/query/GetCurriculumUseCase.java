package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumInfo;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumProgressInfo;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumWeekInfo;
import java.util.List;

/**
 * Curriculum 조회 UseCase
 */
public interface GetCurriculumUseCase {

    CurriculumInfo getByGisuAndPart(Long gisuId, ChallengerPart part, Integer weekNo);

    /**
     * 내 커리큘럼 진행 상황 조회 - 현재 활성 기수의 챌린저 기반으로 워크북 목록 + 제출 현황
     */
    @Deprecated
    CurriculumProgressInfo getMyProgress(Long memberId);

    /**
     * 내 커리큘럼 진행 상황 조회 - 지정한 기수의 챌린저 기반으로 워크북 목록 + 제출 현황
     */
    CurriculumProgressInfo getMyProgressByGisu(Long memberId, Long gisuId);

    /**
     * 파트별 커리큘럼 주차 목록 조회 - 활성 기수의 해당 파트 워크북 주차/제목 목록
     */
    @Deprecated
    List<CurriculumWeekInfo> getWeeksByPart(ChallengerPart part);
}
