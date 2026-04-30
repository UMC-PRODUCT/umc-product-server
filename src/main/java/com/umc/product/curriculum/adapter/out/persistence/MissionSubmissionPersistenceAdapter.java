package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.application.port.out.LoadMissionFeedbackPort;
import com.umc.product.curriculum.application.port.out.LoadMissionSubmissionPort;
import com.umc.product.curriculum.domain.MissionFeedback;
import com.umc.product.curriculum.domain.MissionSubmission;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionSubmissionPersistenceAdapter implements LoadMissionSubmissionPort, LoadMissionFeedbackPort {

    private final MissionSubmissionJpaRepository missionSubmissionJpaRepository;
    private final MissionFeedbackJpaRepository missionFeedbackJpaRepository;

    @Override
    public List<MissionSubmission> findByChallengerWorkbookId(Long challengerWorkbookId) {
        return missionSubmissionJpaRepository.findByChallengerWorkbook_Id(challengerWorkbookId);
    }

    @Override
    public List<MissionFeedback> findByMissionSubmissionIdIn(List<Long> submissionIds) {
        if (submissionIds.isEmpty()) {
            return List.of();
        }
        return missionFeedbackJpaRepository.findByMissionSubmission_IdIn(submissionIds);
    }
}