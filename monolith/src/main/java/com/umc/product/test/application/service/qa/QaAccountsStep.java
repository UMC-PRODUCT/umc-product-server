package com.umc.product.test.application.service.qa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.test.application.port.in.command.CreateSeedMemberSystemRoleUseCase;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberSystemRoleCommand;
import com.umc.product.test.application.port.in.command.dto.QaSeedStep;
import com.umc.product.test.domain.roster.QaAccountPlan.Account;
import com.umc.product.test.domain.roster.QaPersonas;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 계정 593개. 한 명 단위로 커밋하므로 중간에 끊겨도 다시 부르면 없는 사람만 이어서 만든다.
 */
@Slf4j
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class QaAccountsStep implements QaSeedStepRunner {
    private final QaAccountWriter accountWriter;
    private final QaDirectoryLoader directoryLoader;
    private final CreateSeedMemberSystemRoleUseCase createSeedMemberSystemRoleUseCase;

    @Override
    public QaSeedStep step() {
        return QaSeedStep.ACCOUNTS;
    }

    @Override
    public StepTally run(QaDirectory directory) {
        StepTally tally = new StepTally();
        for (Account account : directory.accounts().accounts()) {
            try {
                if (accountWriter.write(account, directory)) {
                    tally.created();
                } else {
                    tally.skipped();
                }
            } catch (RuntimeException e) {
                log.warn("QA 계정 시딩 실패: email={}", account.email(), e);
                tally.failed(SeedFailureCode.of(e));
            }
        }
        Long superAdminId = directoryLoader.load().memberId(QaPersonas.SUPERADMIN);
        boolean granted = createSeedMemberSystemRoleUseCase.create(
            new CreateSeedMemberSystemRoleCommand(superAdminId, MemberSystemRoleType.SUPER_ADMIN)).created();
        if (granted) {
            tally.created();
        }
        return tally;
    }
}
