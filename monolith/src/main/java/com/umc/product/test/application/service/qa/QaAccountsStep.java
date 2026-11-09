package com.umc.product.test.application.service.qa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.umc.product.test.application.port.in.command.CreateSeedMemberSystemRoleUseCase;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberSystemRoleCommand;
import com.umc.product.test.application.port.in.command.dto.QaSeedStep;
import com.umc.product.test.application.service.SeedFailureCode;
import com.umc.product.test.domain.roster.QaAccountPlan.Account;
import com.umc.product.test.domain.roster.QaAccountPlan.SystemRoleGrant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 한 명 단위로 커밋하므로 중간에 끊겨도 다시 부르면 없는 사람만 이어서 만든다. 시스템 역할도 한 건씩 주고, 이미 있으면 건너뛴다.
 */
@Slf4j
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class QaAccountsStep implements QaSeedStepRunner {
    static final String SYSTEM_ROLE_HOLDER_MISSING = "SYSTEM_ROLE_HOLDER_MISSING";

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
        QaDirectory reloaded = directoryLoader.load();
        for (SystemRoleGrant grant : reloaded.accounts().systemRoles()) {
            Long memberId = reloaded.findMemberId(grant.email());
            if (memberId == null) {
                tally.failed(SYSTEM_ROLE_HOLDER_MISSING);
                continue;
            }
            try {
                if (createSeedMemberSystemRoleUseCase.create(
                    new CreateSeedMemberSystemRoleCommand(memberId, grant.role())).created()) {
                    tally.created();
                } else {
                    tally.skipped();
                }
            } catch (RuntimeException e) {
                log.warn("QA 시스템 역할 시딩 실패: email={}, role={}", grant.email(), grant.role(), e);
                tally.failed(SeedFailureCode.of(e));
            }
        }
        return tally;
    }
}
