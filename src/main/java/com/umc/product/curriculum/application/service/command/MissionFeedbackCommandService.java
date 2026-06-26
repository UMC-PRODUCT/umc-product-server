package com.umc.product.curriculum.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.curriculum.application.port.in.command.ManageMissionFeedbackUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.CreateMissionFeedbackCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.DeleteMissionFeedbackCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.EditMissionFeedbackCommand;
import com.umc.product.curriculum.application.port.out.LoadMissionFeedbackPort;
import com.umc.product.curriculum.application.port.out.LoadMissionSubmissionPort;
import com.umc.product.curriculum.application.port.out.SaveMissionFeedbackPort;
import com.umc.product.curriculum.domain.MissionFeedback;
import com.umc.product.curriculum.domain.MissionSubmission;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionFeedbackCommandService implements ManageMissionFeedbackUseCase {

    private final LoadMissionSubmissionPort loadMissionSubmissionPort;
    private final LoadMissionFeedbackPort loadMissionFeedbackPort;
    private final SaveMissionFeedbackPort saveMissionFeedbackPort;

    @Override
    public Long create(CreateMissionFeedbackCommand command) {
        MissionSubmission missionSubmission = loadMissionSubmissionPort.getById(command.missionSubmissionId());

        MissionFeedback missionFeedback = MissionFeedback.create(
            missionSubmission,
            command.reviewerMemberId(),
            command.content(),
            command.result()
        );

        return saveMissionFeedbackPort.save(missionFeedback).getId();
    }

    @Override
    public void edit(EditMissionFeedbackCommand command) {
        MissionFeedback missionFeedback = loadMissionFeedbackPort.getById(command.missionFeedbackId());
        validateReviewer(missionFeedback, command.reviewerMemberId());

        missionFeedback.edit(command.content());
        saveMissionFeedbackPort.save(missionFeedback);
    }

    @Override
    public void delete(DeleteMissionFeedbackCommand command) {
        MissionFeedback missionFeedback = loadMissionFeedbackPort.getById(command.missionFeedbackId());
        validateReviewer(missionFeedback, command.operatorMemberId());

        saveMissionFeedbackPort.delete(missionFeedback);
    }

    private void validateReviewer(MissionFeedback missionFeedback, Long reviewerMemberId) {
        if (!missionFeedback.isReviewedBy(reviewerMemberId)) {
            throw new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_ACCESS_DENIED);
        }
    }
}
