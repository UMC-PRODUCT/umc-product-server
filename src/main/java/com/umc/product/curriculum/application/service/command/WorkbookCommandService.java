package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.ReviewWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.SelectBestWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.SubmitWorkbookCommand;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveChallengerWorkbookPort;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkbookCommandService implements ManageWorkbookUseCase {

    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final SaveChallengerWorkbookPort saveChallengerWorkbookPort;

    @Override
    public void submit(SubmitWorkbookCommand command) {
        ChallengerWorkbook workbook = loadChallengerWorkbookPort.findById(command.challengerWorkbookId());
        OriginalWorkbook originalWorkbook = loadOriginalWorkbookPort.findById(workbook.getOriginalWorkbookId());

        // Github, Notion인데 submission이 없다면 에러
        validateSubmission(originalWorkbook.getMissionType(), command.submission());

        workbook.submit(command.submission());

        saveChallengerWorkbookPort.save(workbook);
    }

    private void validateSubmission(MissionType missionType, String submission) {
        if (missionType != MissionType.PLAIN && !StringUtils.hasText(submission)) {
            throw new BusinessException(Domain.CURRICULUM, CurriculumErrorCode.SUBMISSION_REQUIRED);
        }
    }

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
