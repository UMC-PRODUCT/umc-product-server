package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.MissionSubmission;
import java.util.List;

public interface LoadMissionSubmissionPort {

    /**
     * 챌린저 워크북에 속한 모든 미션 제출물 조회
     * <p>
     * (original_workbook_mission_id, challenger_workbook_id) UNIQUE 제약으로 미션 당 최대 1건
     */
    List<MissionSubmission> findByChallengerWorkbookId(Long challengerWorkbookId);

    /**
     * 여러 챌린저 워크북에 속한 미션 제출물 일괄 조회 (N+1 방지)
     */
    List<MissionSubmission> findByChallengerWorkbookIdIn(List<Long> challengerWorkbookIds);
}