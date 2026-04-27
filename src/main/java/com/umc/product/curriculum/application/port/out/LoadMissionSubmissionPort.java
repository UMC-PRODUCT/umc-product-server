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
}