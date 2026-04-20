package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageMissionSubmissionUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.CreateMissionSubmissionCommand;
import com.umc.product.curriculum.application.port.in.command.dto.DeleteMissionSubmissionCommand;
import com.umc.product.curriculum.application.port.in.command.dto.EditMissionSubmissionCommand;
import com.umc.product.global.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionSubmissionCommandService implements ManageMissionSubmissionUseCase {

    @Override
    public Long createSubmission(CreateMissionSubmissionCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void editSubmission(EditMissionSubmissionCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteSubmission(DeleteMissionSubmissionCommand command) {
        throw new NotImplementedException();
    }
}