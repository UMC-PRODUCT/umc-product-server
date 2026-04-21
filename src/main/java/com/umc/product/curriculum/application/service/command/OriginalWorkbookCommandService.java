package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.AutoReleaseWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageOriginalWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.ChangeOriginalWorkbookStatusCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.CreateOriginalWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.EditOriginalWorkbookCommand;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveOriginalWorkbookPort;
import com.umc.product.global.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OriginalWorkbookCommandService implements ManageOriginalWorkbookUseCase, AutoReleaseWorkbookUseCase {

    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final SaveOriginalWorkbookPort saveOriginalWorkbookPort;

    @Override
    public Long create(CreateOriginalWorkbookCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void edit(EditOriginalWorkbookCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(Long originalWorkbookId) {
        throw new NotImplementedException();
    }

    @Override
    public void changeStatusForRelease(List<ChangeOriginalWorkbookStatusCommand> commands) {
        throw new NotImplementedException();
    }

}
