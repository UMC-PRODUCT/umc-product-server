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
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
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
        OriginalWorkbook originalWorkbook = loadOriginalWorkbookPort.findById(command.originalWorkbookId());

        // Github, Notion인데 submission이 없다면 에러
        validateSubmission(originalWorkbook.getMissionType(), command.submission());

        // 이미 제출한 이력이 있는지 확인
        if (!loadChallengerWorkbookPort.findAllByChallengerIdAndOriginalWorkbookId(
            command.challengerId(),
            originalWorkbook.getId()
        ).isEmpty()) {
            throw new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_SUBMISSION_ALREADY_EXISTS);
        }

        ChallengerWorkbook challengerWorkbook = ChallengerWorkbook.create(
            command.challengerId(),
            originalWorkbook.getId(),
            WorkbookStatus.PENDING,
            null
        );

        challengerWorkbook.submit(command.submission());

        saveChallengerWorkbookPort.save(challengerWorkbook);
    }

    /**
     * 미션 유형이 PLAIN, 즉 단순 제출이 아닌 경우에 submission이 없는 경우를 검증합니다.
     */
    private void validateSubmission(MissionType missionType, String submission) {
        if (missionType != MissionType.PLAIN && !StringUtils.hasText(submission)) {
            throw new CurriculumDomainException(CurriculumErrorCode.SUBMISSION_REQUIRED);
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

        workbook.selectBest(command.bestReason());

        saveChallengerWorkbookPort.save(workbook);
    }
}
