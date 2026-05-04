package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.adapter.out.persistence.repository.MissionFeedbackJpaRepository;
import com.umc.product.curriculum.adapter.out.persistence.repository.MissionSubmissionJpaRepository;
import com.umc.product.curriculum.application.port.out.LoadMissionFeedbackPort;
import com.umc.product.curriculum.application.port.out.LoadMissionSubmissionPort;
import com.umc.product.curriculum.domain.MissionFeedback;
import com.umc.product.curriculum.domain.MissionSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MissionSubmissionPersistenceAdapter implements LoadMissionSubmissionPort, LoadMissionFeedbackPort {

    private final MissionSubmissionJpaRepository missionSubmissionJpaRepository;
    private final MissionFeedbackJpaRepository missionFeedbackJpaRepository;

    @Override
    public List<MissionSubmission> findByChallengerWorkbookIdIn(List<Long> challengerWorkbookIds) {
        if (challengerWorkbookIds.isEmpty()) {
            return List.of();
        }
        return missionSubmissionJpaRepository.findByChallengerWorkbook_IdIn(challengerWorkbookIds);
    }

    @Override
    public List<MissionFeedback> findByMissionSubmissionIdIn(List<Long> submissionIds) {
        if (submissionIds.isEmpty()) {
            return List.of();
        }
        return missionFeedbackJpaRepository.findByMissionSubmission_IdIn(submissionIds);
    }
}
