package com.umc.product.curriculum.application.port.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.CurriculumProgressInfo;

public interface LoadCurriculumProgressPort {

    /**
     * 챌린저의 커리큘럼 진행 상황 조회 (활성 기수 기준)
     */
    CurriculumProgressInfo findCurriculumProgress(Long challengerId, ChallengerPart part);
}
