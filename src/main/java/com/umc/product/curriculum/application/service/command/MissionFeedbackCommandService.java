package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageMissionFeedbackUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.CreateMissionFeedbackCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.DeleteMissionFeedbackCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.EditMissionFeedbackCommand;
import com.umc.product.global.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionFeedbackCommandService implements ManageMissionFeedbackUseCase {

    @Override
    public Long createFeedback(CreateMissionFeedbackCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void editFeedback(EditMissionFeedbackCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteFeedback(DeleteMissionFeedbackCommand command) {
        throw new NotImplementedException();
    }
}
