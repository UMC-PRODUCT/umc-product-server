package com.umc.product.curriculum.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.umc.product.curriculum.application.port.out.LoadMissionSubmissionPort;
import com.umc.product.curriculum.application.port.out.SaveMissionSubmissionPort;
import com.umc.product.curriculum.domain.MissionSubmission;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MissionSubmissionPersistenceAdapter implements LoadMissionSubmissionPort, SaveMissionSubmissionPort {

    private final MissionSubmissionJpaRepository missionSubmissionJpaRepository;

    @Override
    public MissionSubmission getById(Long missionSubmissionId) {
        return missionSubmissionJpaRepository.findById(missionSubmissionId)
            .orElseThrow(() -> new CurriculumDomainException(CurriculumErrorCode.MISSION_SUBMISSION_NOT_FOUND));
    }

    @Override
    public List<MissionSubmission> findByChallengerWorkbookId(Long challengerWorkbookId) {
        return missionSubmissionJpaRepository.findByChallengerWorkbook_Id(challengerWorkbookId);
    }

    @Override
    public List<MissionSubmission> findByChallengerWorkbookIdIn(List<Long> challengerWorkbookIds) {
        if (challengerWorkbookIds.isEmpty()) {
            return List.of();
        }
        return missionSubmissionJpaRepository.findByChallengerWorkbook_IdIn(challengerWorkbookIds);
    }

    @Override
    public boolean existsByOriginalWorkbookMissionId(Long originalWorkbookMissionId) {
        return missionSubmissionJpaRepository.existsByOriginalWorkbookMission_Id(originalWorkbookMissionId);
    }

    @Override
    public boolean existsByOriginalWorkbookMissionIdAndChallengerWorkbookId(
        Long originalWorkbookMissionId,
        Long challengerWorkbookId
    ) {
        return missionSubmissionJpaRepository.existsByOriginalWorkbookMission_IdAndChallengerWorkbook_Id(
            originalWorkbookMissionId,
            challengerWorkbookId
        );
    }

    @Override
    public MissionSubmission save(MissionSubmission missionSubmission) {
        return missionSubmissionJpaRepository.save(missionSubmission);
    }

    @Override
    public void delete(MissionSubmission missionSubmission) {
        missionSubmissionJpaRepository.delete(missionSubmission);
    }
}
