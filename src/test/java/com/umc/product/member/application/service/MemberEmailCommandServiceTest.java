package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.member.application.port.in.command.dto.ChangeMemberEmailCommand;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberEmailCommandService")
class MemberEmailCommandServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final String OLD_EMAIL = "old@example.com";
    private static final String NEW_EMAIL = "new@example.com";

    @Mock
    LoadMemberPort loadMemberPort;

    @InjectMocks
    MemberEmailCommandService service;

    @Test
    @DisplayName("새 이메일이 사용 가능하면 회원 이메일을 변경한다")
    void changeEmail_success() {
        // given
        Member member = member(OLD_EMAIL);
        given(loadMemberPort.findByIdForUpdate(MEMBER_ID)).willReturn(Optional.of(member));
        given(loadMemberPort.existsByEmail(NEW_EMAIL)).willReturn(false);

        // when
        service.changeEmail(ChangeMemberEmailCommand.of(MEMBER_ID, NEW_EMAIL));

        // then
        assertThat(member.getEmail()).isEqualTo(NEW_EMAIL);
    }

    @Test
    @DisplayName("현재 이메일과 새 이메일이 같으면 중복 조회 없이 성공 처리한다")
    void changeEmail_sameEmail_noop() {
        // given
        Member member = member(OLD_EMAIL);
        given(loadMemberPort.findByIdForUpdate(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        service.changeEmail(ChangeMemberEmailCommand.of(MEMBER_ID, OLD_EMAIL));

        // then
        assertThat(member.getEmail()).isEqualTo(OLD_EMAIL);
        then(loadMemberPort).should(never()).existsByEmail(OLD_EMAIL);
    }

    @Test
    @DisplayName("새 이메일이 이미 사용 중이면 EMAIL_ALREADY_EXISTS 예외를 던지고 변경하지 않는다")
    void changeEmail_duplicateEmail_rejected() {
        // given
        Member member = member(OLD_EMAIL);
        given(loadMemberPort.findByIdForUpdate(MEMBER_ID)).willReturn(Optional.of(member));
        given(loadMemberPort.existsByEmail(NEW_EMAIL)).willReturn(true);

        // when / then
        assertThatThrownBy(() -> service.changeEmail(ChangeMemberEmailCommand.of(MEMBER_ID, NEW_EMAIL)))
            .isInstanceOf(MemberDomainException.class)
            .extracting("baseCode")
            .isEqualTo(MemberErrorCode.EMAIL_ALREADY_EXISTS);

        assertThat(member.getEmail()).isEqualTo(OLD_EMAIL);
    }

    @Test
    @DisplayName("회원이 없으면 MEMBER_NOT_FOUND 예외를 던진다")
    void changeEmail_memberNotFound_rejected() {
        // given
        given(loadMemberPort.findByIdForUpdate(MEMBER_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.changeEmail(ChangeMemberEmailCommand.of(MEMBER_ID, NEW_EMAIL)))
            .isInstanceOf(MemberDomainException.class)
            .extracting("baseCode")
            .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

        then(loadMemberPort).should(never()).existsByEmail(NEW_EMAIL);
    }

    private Member member(String email) {
        Member member = Member.create("홍길동", "길동", email, 1L, null);
        ReflectionTestUtils.setField(member, "id", MEMBER_ID);
        return member;
    }
}
