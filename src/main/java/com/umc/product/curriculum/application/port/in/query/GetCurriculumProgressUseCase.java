package com.umc.product.curriculum.application.port.in.query;

public interface GetCurriculumProgressUseCase {

    /**
     * 챌린저의 커리큘럼 진행 상황 조회 - 챌린저의 기수/파트에 해당하는 워크북 목록 + 제출 현황
     */
    CurriculumProgressInfo getMyProgress(Long challengerId);
}
