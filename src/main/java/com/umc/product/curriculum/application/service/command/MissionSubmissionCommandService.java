package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageMissionSubmissionUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.CreateMissionSubmissionCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.DeleteMissionSubmissionCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.EditMissionSubmissionCommand;
import com.umc.product.global.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionSubmissionCommandService implements ManageMissionSubmissionUseCase {

    @Override
    public Long create(CreateMissionSubmissionCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void edit(EditMissionSubmissionCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void withdraw(DeleteMissionSubmissionCommand command) {
        throw new NotImplementedException();
    }
}
