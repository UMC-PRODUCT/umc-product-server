package com.umc.product.test.application.service.qa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.test.adapter.out.resource.ClasspathQaRosterAdapter;
import com.umc.product.test.application.port.in.command.CreateSeedMemberSystemRoleUseCase;
import com.umc.product.test.domain.roster.QaAccountPlan;
import com.umc.product.test.domain.roster.QaAccountPlan.Account;
import com.umc.product.test.domain.roster.QaPersonas;
import com.umc.product.test.domain.roster.QaRoster;

@ExtendWith(MockitoExtension.class)
@DisplayName("QA 계정 단계")
class QaAccountsStepTest {

    @Mock
    private QaAccountWriter accountWriter;
    @Mock
    private QaDirectoryLoader directoryLoader;
    @Mock
    private CreateSeedMemberSystemRoleUseCase createSeedMemberSystemRoleUseCase;
    @InjectMocks
    private QaAccountsStep sut;

    @Test
    @DisplayName("superadmin 계정을 만들지 못하면 권한 부여만 건너뛰고 앞에서 만든 수와 실패 코드를 남긴다")
    void superadminMissingKeepsTally() {
        // given
        QaRoster roster = new ClasspathQaRosterAdapter().load();
        QaAccountPlan accounts = QaAccountPlan.from(roster);
        QaDirectory empty = new QaDirectory(roster, accounts, Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        String superadminEmail = QaPersonas.SUPERADMIN.email(roster);
        given(accountWriter.write(any(), any())).willAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            if (account.email().equals(superadminEmail)) {
                throw new IllegalStateException("superadmin 생성 실패");
            }
            return true;
        });
        given(directoryLoader.load()).willReturn(empty);

        // when
        StepTally tally = sut.run(empty);

        // then
        assertThat(tally.createdCount()).isEqualTo(accounts.accounts().size() - 1);
        assertThat(tally.failedByCode()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "IllegalStateException", 1,
            QaAccountsStep.SUPERADMIN_MISSING, 1));
        verify(createSeedMemberSystemRoleUseCase, never()).create(any());
    }
}
