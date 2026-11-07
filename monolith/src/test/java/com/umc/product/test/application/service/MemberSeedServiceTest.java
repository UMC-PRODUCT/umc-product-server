package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.member.application.port.in.command.RegisterEmailMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.EmailRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberCommand;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberResult;

@ExtendWith(MockitoExtension.class)
class MemberSeedServiceTest {

    @Mock
    RegisterEmailMemberUseCase registerEmailMemberUseCase;
    @Mock
    DummyMemberFactory dummyMemberFactory;

    MemberSeedService sut;
    List<TermConsents> consents;

    @BeforeEach
    void setUp() {
        consents = List.of(TermConsents.builder().termId(1L).isAgreed(true).build());
        SeedProperties properties = new SeedProperties(true, "alpha.umc.test", "Alpha!Pass2026");
        sut = new MemberSeedService(properties, dummyMemberFactory, registerEmailMemberUseCase);
        given(dummyMemberFactory.snapshotMandatoryConsents()).willReturn(consents);
        given(registerEmailMemberUseCase.register(any())).willReturn(7L);
    }

    @Test
    @DisplayName("비밀번호를 비워 보내면 기본 비밀번호와 활성 필수 약관 동의로 회원을 만든다")
    void 비밀번호를_비우면_기본_비밀번호를_쓴다() {
        // When
        CreateSeedMemberResult result = sut.create(
            CreateSeedMemberCommand.of("이름", "닉네임", 3L, "seed@alpha.umc.test", " "));

        // Then
        EmailRegisterMemberCommand registered = registeredCommand();
        assertThat(result.memberId()).isEqualTo(7L);
        assertThat(registered.rawPassword()).isEqualTo("Alpha!Pass2026");
        assertThat(registered.termConsents()).isEqualTo(consents);
        assertThat(registered.schoolId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("비밀번호를 보내면 그 비밀번호로 회원을 만든다")
    void 비밀번호를_보내면_그대로_쓴다() {
        // When
        sut.create(CreateSeedMemberCommand.of("이름", "닉네임", 3L, "seed@alpha.umc.test", "Custom!Pass1"));

        // Then
        assertThat(registeredCommand().rawPassword()).isEqualTo("Custom!Pass1");
    }

    private EmailRegisterMemberCommand registeredCommand() {
        ArgumentCaptor<EmailRegisterMemberCommand> captor = ArgumentCaptor.forClass(EmailRegisterMemberCommand.class);
        verify(registerEmailMemberUseCase).register(captor.capture());
        return captor.getValue();
    }
}
