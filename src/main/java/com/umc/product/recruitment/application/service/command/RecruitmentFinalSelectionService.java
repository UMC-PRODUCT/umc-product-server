package com.umc.product.recruitment.application.service.command;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.application.port.in.command.UpdateFinalStatusUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateFinalStatusCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateFinalStatusResult;
import com.umc.product.recruitment.application.port.out.LoadApplicationPartPreferencePort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewAssignmentPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentSchedulePort;
import com.umc.product.recruitment.application.port.out.SaveApplicationPort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.InterviewAssignment;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.PartKey;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitmentFinalSelectionService implements UpdateFinalStatusUseCase {

    private final LoadApplicationPort loadApplicationPort;
    private final SaveApplicationPort saveApplicationPort;
    private final LoadApplicationPartPreferencePort loadApplicationPartPreferencePort;
    private final LoadRecruitmentPort loadRecruitmentPort;
    private final LoadInterviewAssignmentPort loadInterviewAssignmentPort;
    private final LoadRecruitmentSchedulePort loadRecruitmentSchedulePort;

    @Override
    @Transactional
    public UpdateFinalStatusResult update(UpdateFinalStatusCommand command) {

        validateFinalResultNotPublished(command.recruitmentId());

        Recruitment currentRecruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        Long rootId = currentRecruitment.getEffectiveRootId();

        Application application = loadApplicationPort.findById(command.applicationId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.APPLICATION_NOT_FOUND));

        validateInterviewProcessFinished(application);

        if (!application.getRecruitment().getEffectiveRootId().equals(rootId)) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.APPLICATION_NOT_BELONGS_TO_RECRUITMENT);
        }

        // todo: 운영진 권한 및 학교 체크

        switch (command.evaluationDecision()) {
            case PASS -> {
                ChallengerPart selectedPart = command.selectedPart();

                if (selectedPart != null && !loadApplicationPartPreferencePort.existsPreferredOpenPart(
                    application.getId(),
                    selectedPart
                )) {
                    throw new BusinessException(Domain.RECRUITMENT,
                        RecruitmentErrorCode.FINAL_SELECTED_PART_NOT_PREFERRED);
                }
                application.passFinal(selectedPart);
            }
            case FAIL -> {
                application.failFinal();
            }
            case WAIT -> {
                application.resetFinalDecision();
            }
        }

        saveApplicationPort.save(application);

        return new UpdateFinalStatusResult(
            application.getId(),
            new UpdateFinalStatusResult.FinalResult(
                command.evaluationDecision().name(),
                toPartKey(application.getSelectedPart())
            )
        );
    }

    private PartKey toPartKey(ChallengerPart selectedPart) {
        if (selectedPart == null) {
            return null;
        }
        return PartKey.valueOf(selectedPart.name());
    }

    private void validateInterviewProcessFinished(Application application) {
        // 면접 배정이 아예 없는 경우
        InterviewAssignment assignment = loadInterviewAssignmentPort.findByApplicationId(application.getId())
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_FOUND));

        // 아직 면접 시간이 끝나지 않은 경우 - 슬롯 시간이 지난 후에만 최종합격 가능
        if (assignment.getSlot().getEndsAt().isAfter(Instant.now())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_NOT_FINISHED_YET);
        }

    }

    private void validateFinalResultNotPublished(Long recruitmentId) {
        RecruitmentSchedule finalResultAt = loadRecruitmentSchedulePort.findByRecruitmentIdAndType(recruitmentId,
            RecruitmentScheduleType.FINAL_RESULT_AT);

        // 최종 결과 발표 시점이 지났다면(isActive) 평가 제출/재제출 불가
        if (finalResultAt != null && finalResultAt.isActive(java.time.Instant.now())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.FINAL_RESULT_ALREADY_PUBLISHED);
        }
    }
}
