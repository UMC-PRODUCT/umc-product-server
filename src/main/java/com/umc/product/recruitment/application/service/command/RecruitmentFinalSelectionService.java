package com.umc.product.recruitment.application.service.command;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.application.port.in.command.UpdateFinalStatusUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateFinalStatusCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateFinalStatusResult;
import com.umc.product.recruitment.application.port.out.LoadApplicationPartPreferencePort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.SaveApplicationPort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.enums.PartKey;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitmentFinalSelectionService implements UpdateFinalStatusUseCase {

    private final LoadApplicationPort loadApplicationPort;
    private final SaveApplicationPort saveApplicationPort;
    private final LoadApplicationPartPreferencePort loadApplicationPartPreferencePort;

    @Override
    @Transactional
    public UpdateFinalStatusResult update(UpdateFinalStatusCommand command) {
        Application application = loadApplicationPort.getByRecruitmentIdAndApplicationId(
            command.recruitmentId(),
            command.applicationId()
        ).orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.APPLICATION_NOT_FOUND));

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

}
