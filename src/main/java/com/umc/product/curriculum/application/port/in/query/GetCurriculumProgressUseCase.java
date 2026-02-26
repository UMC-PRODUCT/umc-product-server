package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

public interface GetCurriculumProgressUseCase {

    /**
     * 내 커리큘럼 진행 상황 조회 - 현재 활성 기수의 챌린저 기반으로 워크북 목록 + 제출 현황
     */
    CurriculumProgressInfo getMyProgress(Long memberId);

    /**
     * 파트별 커리큘럼 주차 목록 조회 - 활성 기수의 해당 파트 워크북 주차/제목 목록
     */
    List<CurriculumWeekInfo> getWeeksByPart(ChallengerPart part);
}
