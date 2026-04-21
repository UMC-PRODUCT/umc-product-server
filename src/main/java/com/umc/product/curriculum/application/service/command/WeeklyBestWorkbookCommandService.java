package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageWeeklyBestWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.CreateWeeklyBestWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.EditWeeklyBestWorkbookCommand;
import com.umc.product.global.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WeeklyBestWorkbookCommandService implements ManageWeeklyBestWorkbookUseCase {

    @Override
    public void createWeeklyBestWorkbook(CreateWeeklyBestWorkbookCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void editWeeklyBestWorkbookReason(EditWeeklyBestWorkbookCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteWeeklyBestWorkbook(Long weeklyBestWorkbookId) {
        throw new NotImplementedException();
    }
}
