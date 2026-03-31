package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageChallengerWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.ReviewWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.SelectBestWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.SubmitChallengerWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.SubmitWorkbookCommand;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveChallengerWorkbookPort;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengerWorkbookCommandService implements ManageChallengerWorkbookUseCase {

    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final SaveChallengerWorkbookPort saveChallengerWorkbookPort;
    private final GetChallengerUseCase getChallengerUseCase;

    @Override
    public void submit(SubmitWorkbookCommand command) {
        OriginalWorkbook originalWorkbook = loadOriginalWorkbookPort.findById(command.originalWorkbookId());

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

        challengerWorkbook.submit(originalWorkbook.getMissionType(), command.submission());

        saveChallengerWorkbookPort.save(challengerWorkbook);
    }

    @Override
    public void submitByWorkbookId(SubmitChallengerWorkbookCommand command) {
        ChallengerWorkbook challengerWorkbook = loadChallengerWorkbookPort.findById(command.challengerWorkbookId());

        // 본인 워크북인지 확인 (challengerId → memberId 비교)
        verifyWorkbookOwner(challengerWorkbook, command.memberId());

        OriginalWorkbook originalWorkbook = loadOriginalWorkbookPort.findById(challengerWorkbook.getOriginalWorkbookId());

        challengerWorkbook.submit(originalWorkbook.getMissionType(), command.submission());

        saveChallengerWorkbookPort.save(challengerWorkbook);
    }

    @Override
    public void review(ReviewWorkbookCommand command) {
        ChallengerWorkbook workbook = loadChallengerWorkbookPort.findById(command.challengerWorkbookId());

        workbook.review(command.status(), command.feedback());

        saveChallengerWorkbookPort.save(workbook);
    }

    @Override
    public void selectBest(SelectBestWorkbookCommand command) {
        ChallengerWorkbook workbook = loadChallengerWorkbookPort.findById(command.challengerWorkbookId());

        workbook.selectBest(command.bestReason());

        saveChallengerWorkbookPort.save(workbook);
    }

    private void verifyWorkbookOwner(ChallengerWorkbook workbook, Long memberId) {
        ChallengerInfo challenger = getChallengerUseCase.getChallengerPublicInfo(workbook.getChallengerId());
        if (!challenger.memberId().equals(memberId)) {
            throw new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_ACCESS_DENIED);
        }
    }
}
