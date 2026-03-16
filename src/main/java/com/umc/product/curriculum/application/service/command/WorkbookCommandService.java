package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.AutoReleaseWorkbookUseCase;
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
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkbookCommandService implements ManageWorkbookUseCase, AutoReleaseWorkbookUseCase {

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

    /**
     * 배포 조건을 만족하는 모든 워크북을 일괄 배포합니다.
     * <p>
     * 연쇄 배포 방지: 조회를 먼저 완료한 후 배포를 수행합니다.
     */
    @Override
    public int releaseAllDue() {
        Instant now = Instant.now();

        // 1. 미배포 & 시작일 지난 워크북 전부 조회 (연쇄 배포 방지)
        List<OriginalWorkbook> candidates = loadOriginalWorkbookPort.findUnreleasedWithStartDateBefore(now);

        if (candidates.isEmpty()) {
            log.info("[WorkbookAutoRelease] 배포 대상 워크북 없음");
            return 0;
        }

        // 2. 직전 주차 배포 여부 확인하여 필터링
        List<OriginalWorkbook> toRelease = candidates.stream()
            .filter(this::isPreviousWeekReleased)
            .toList();

        if (toRelease.isEmpty()) {
            log.info("[WorkbookAutoRelease] 직전 주차 미배포로 배포 대상 없음 (후보: {}건)", candidates.size());
            return 0;
        }

        // 3. 한 번에 배포
        for (OriginalWorkbook workbook : toRelease) {
            workbook.release();
            log.info("[WorkbookAutoRelease] 워크북 배포 완료: id={}, weekNo={}, title={}",
                workbook.getId(), workbook.getWeekNo(), workbook.getTitle());
        }

        log.info("[WorkbookAutoRelease] 총 {}건 배포 완료", toRelease.size());
        return toRelease.size();
    }

    /**
     * 직전 주차가 배포되었는지 확인합니다. 1주차는 직전 주차가 없으므로 true를 반환합니다.
     */
    private boolean isPreviousWeekReleased(OriginalWorkbook workbook) {
        if (workbook.getWeekNo() == 1) {
            return true;
        }

        return loadOriginalWorkbookPort
            .findByCurriculumIdAndWeekNo(
                workbook.getCurriculum().getId(),
                workbook.getWeekNo() - 1
            )
            .map(OriginalWorkbook::isReleased)
            .orElse(false);
    }
}
