package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageOriginalWorkbookMissionUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.EditOriginalWorkbookMissionCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.CreateOriginalWorkbookMissionCommand;
import com.umc.product.global.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OriginalWorkbookMissionCommandService implements ManageOriginalWorkbookMissionUseCase {

    @Override
    public Long create(CreateOriginalWorkbookMissionCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void edit(EditOriginalWorkbookMissionCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(Long originalMissionId) {
        throw new NotImplementedException();
    }
}
