package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageAdminChallengerWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.ReviewWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.SelectBestWorkbookCommand;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveChallengerWorkbookPort;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminChallengerWorkbookCommandService implements ManageAdminChallengerWorkbookUseCase {

    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final SaveChallengerWorkbookPort saveChallengerWorkbookPort;

    @Override
    public void review(ReviewWorkbookCommand command) {
        ChallengerWorkbook workbook = loadChallengerWorkbookPort.findById(command.challengerWorkbookId());

        if (command.status() == WorkbookStatus.PASS) {
            workbook.markAsPass(command.feedback());
        } else if (command.status() == WorkbookStatus.FAIL) {
            workbook.markAsFail(command.feedback());
        }

        saveChallengerWorkbookPort.save(workbook);
    }

    @Override
    public void selectBest(SelectBestWorkbookCommand command) {
        ChallengerWorkbook workbook = loadChallengerWorkbookPort.findById(command.challengerWorkbookId());

        workbook.selectBest(command.recommendation());

        saveChallengerWorkbookPort.save(workbook);
    }

}
