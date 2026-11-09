package com.umc.product.test.application.service.qa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.test.adapter.out.resource.ClasspathQaRosterAdapter;
import com.umc.product.test.application.port.in.command.CreateSeedMemberSystemRoleUseCase;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberSystemRoleCommand;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberSystemRoleResult;
import com.umc.product.test.domain.roster.QaAccountPlan;
import com.umc.product.test.domain.roster.QaAccountPlan.Account;
import com.umc.product.test.domain.roster.QaPersonas;
import com.umc.product.test.domain.roster.QaRoster;

@ExtendWith(MockitoExtension.class)
@DisplayName("QA 계정 단계")
class QaAccountsStepTest {

    private static final int GRANTS = 1 + 18;

    @Mock
    private QaAccountWriter accountWriter;
    @Mock
    private QaDirectoryLoader directoryLoader;
    @Mock
    private CreateSeedMemberSystemRoleUseCase createSeedMemberSystemRoleUseCase;
    @InjectMocks
    private QaAccountsStep sut;

    private final ClasspathQaRosterAdapter adapter = new ClasspathQaRosterAdapter();
    private final QaRoster roster = adapter.load();
    private final QaAccountPlan accounts = QaAccountPlan.from(roster, adapter.loadSystemRoles());
    private final String superadminEmail = QaPersonas.SUPERADMIN.email(roster);

    @Test
    @DisplayName("superadmin 계정을 만들지 못하면 그 계정의 권한 부여만 건너뛰고 나머지 시스템 역할은 준다")
    void missingHolderSkipsOnlyItsGrant() {
        // given
        given(accountWriter.write(any(), any())).willAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            if (account.email().equals(superadminEmail)) {
                throw new IllegalStateException("superadmin 생성 실패");
            }
            return true;
        });
        Map<String, Long> memberIds = memberIds();
        Long superadminId = memberIds.remove(superadminEmail);
        given(directoryLoader.load()).willReturn(directory(memberIds));
        given(createSeedMemberSystemRoleUseCase.create(any())).willAnswer(invocation -> granted(invocation, true));

        // when
        StepTally tally = sut.run(directory(Map.of()));

        // then
        assertThat(tally.createdCount()).isEqualTo(accounts.accounts().size() - 1 + GRANTS - 1);
        assertThat(tally.failedByCode()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "IllegalStateException", 1,
            QaAccountsStep.SYSTEM_ROLE_HOLDER_MISSING, 1));
        verify(createSeedMemberSystemRoleUseCase, never())
            .create(new CreateSeedMemberSystemRoleCommand(superadminId, MemberSystemRoleType.SUPER_ADMIN));
    }

    @Test
    @DisplayName("계정과 시스템 역할이 이미 모두 있으면 하나도 만들지 않고 전부 건너뛴 것으로 센다")
    void secondRunSkipsEverything() {
        // given
        given(accountWriter.write(any(), any())).willReturn(false);
        given(directoryLoader.load()).willReturn(directory(memberIds()));
        given(createSeedMemberSystemRoleUseCase.create(any())).willAnswer(invocation -> granted(invocation, false));

        // when
        StepTally tally = sut.run(directory(memberIds()));

        // then
        assertThat(tally.createdCount()).isZero();
        assertThat(tally.skippedCount()).isEqualTo(accounts.accounts().size() + GRANTS);
        assertThat(tally.failedByCode()).isEmpty();
    }

    private Map<String, Long> memberIds() {
        Map<String, Long> ids = new HashMap<>();
        for (String email : accounts.emails()) {
            ids.put(email, (long) ids.size() + 1);
        }
        return ids;
    }

    private QaDirectory directory(Map<String, Long> memberIds) {
        return new QaDirectory(roster, accounts, memberIds, Map.of(), Map.of(), Map.of(), Map.of());
    }

    private static CreateSeedMemberSystemRoleResult granted(InvocationOnMock invocation, boolean created) {
        CreateSeedMemberSystemRoleCommand command = invocation.getArgument(0);
        return CreateSeedMemberSystemRoleResult.of(command.memberId(), command.roleType(), created);
    }
}
