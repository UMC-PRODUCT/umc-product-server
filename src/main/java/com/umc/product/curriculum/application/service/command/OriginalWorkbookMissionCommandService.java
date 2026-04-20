package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageOriginalWorkbookMissionUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.CreateOriginalWorkbookMissionCommand;
import com.umc.product.curriculum.application.port.in.command.dto.EditOriginalWorkbookMissionCommand;
import com.umc.product.global.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OriginalWorkbookMissionCommandService implements ManageOriginalWorkbookMissionUseCase {

    @Override
    public Long createOriginalMission(CreateOriginalWorkbookMissionCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void editOriginalMission(EditOriginalWorkbookMissionCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteOriginalMission(Long originalMissionId) {
        throw new NotImplementedException();
    }
}
