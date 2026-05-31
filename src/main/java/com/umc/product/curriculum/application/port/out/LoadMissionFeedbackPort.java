package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.MissionFeedback;
import java.util.List;

public interface LoadMissionFeedbackPort {

    /**
     * 여러 미션 제출물에 대한 피드백 일괄 조회 (N+1 방지)
     */
    List<MissionFeedback> findByMissionSubmissionIdIn(List<Long> submissionIds);
}