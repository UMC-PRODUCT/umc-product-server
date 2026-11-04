package com.umc.product.test.application.service.qa;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.project.application.port.in.command.TransferMatchingChapterUseCase;
import com.umc.product.project.application.port.in.command.dto.TransferMatchingChapterCommand;
import com.umc.product.project.application.port.in.query.GetMyMatchingAffiliationUseCase;
import com.umc.product.test.application.port.in.command.dto.QaSeedStep;
import com.umc.product.test.domain.roster.QaAccountPlan;
import com.umc.product.test.domain.roster.QaPersonas;
import com.umc.product.test.domain.roster.QaTransfers;
import com.umc.product.test.domain.roster.QaTransfers.QaTransfer;

import lombok.RequiredArgsConstructor;

/**
 * 매칭 지부 이동 4명. 활성 이동이 이미 있는 사람은 건너뛰고, 나머지를 운영 이동 UseCase로 superadmin이 한 번에 등록한다.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class QaProjectTransfersStep implements QaSeedStepRunner {

    private static final String REASON = "[QA] 매칭 지부 이동 확인";

    private final GetMyMatchingAffiliationUseCase getMyMatchingAffiliationUseCase;
    private final TransferMatchingChapterUseCase transferMatchingChapterUseCase;

    @Override
    public QaSeedStep step() {
        return QaSeedStep.PROJECT_TRANSFERS;
    }

    @Override
    @Transactional
    public StepTally run(QaDirectory directory) {
        Long gisuId = directory.gisuId(QaAccountPlan.CURRENT_GENERATION);
        StepTally tally = new StepTally();
        List<TransferMatchingChapterCommand.Target> targets = new ArrayList<>();
        for (QaTransfer transfer : QaTransfers.ALL) {
            Long memberId = directory.memberId(new QaPersonas.RosterSeat(transfer.member()));
            if (getMyMatchingAffiliationUseCase.get(memberId, gisuId).transferred()) {
                tally.skipped();
                continue;
            }
            Long toChapterId = directory.chapterId(QaAccountPlan.CURRENT_GENERATION, transfer.toChapter());
            targets.add(new TransferMatchingChapterCommand.Target(memberId, toChapterId, REASON));
        }
        if (!targets.isEmpty()) {
            transferMatchingChapterUseCase.transfer(TransferMatchingChapterCommand.builder()
                .requesterMemberId(directory.memberId(QaPersonas.SUPERADMIN))
                .gisuId(gisuId).targets(targets).build());
            tally.created(targets.size());
        }
        return tally;
    }
}
