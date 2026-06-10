package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.common.domain.enums.MemberRoleType;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberRoleCommand;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberRoleCommandServiceTest {

    @Mock
    LoadMemberPort loadMemberPort;

    @Mock
    SaveMemberPort saveMemberPort;

    @Captor
    ArgumentCaptor<Member> memberCaptor;

    @InjectMocks
    MemberRoleCommandService sut;

    @Test
    @DisplayName("ADMIN은 다른 회원의 권한을 변경할 수 있다")
    void ADMIN은_다른_회원_권한을_변경한다() {
        Member requester = member(1L, MemberRoleType.ADMIN);
        Member target = member(2L, MemberRoleType.NORMAL);
        given(loadMemberPort.findById(1L)).willReturn(Optional.of(requester));
        given(loadMemberPort.findById(2L)).willReturn(Optional.of(target));

        sut.updateRole(UpdateMemberRoleCommand.of(1L, 2L, MemberRoleType.ADMIN));

        then(saveMemberPort).should().save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getRoleType()).isEqualTo(MemberRoleType.ADMIN);
    }

    @Test
    @DisplayName("NORMAL은 다른 회원의 권한을 변경할 수 없다")
    void NORMAL은_권한을_변경할_수_없다() {
        Member requester = member(1L, MemberRoleType.NORMAL);
        given(loadMemberPort.findById(1L)).willReturn(Optional.of(requester));

        assertThatThrownBy(() -> sut.updateRole(UpdateMemberRoleCommand.of(1L, 2L, MemberRoleType.ADMIN)))
            .isInstanceOf(MemberDomainException.class)
            .extracting("baseCode")
            .isEqualTo(MemberErrorCode.MEMBER_ROLE_ACCESS_DENIED);

        then(saveMemberPort).should(never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("ADMIN은 자기 자신을 NORMAL로 강등할 수 없다")
    void ADMIN은_자기_자신을_강등할_수_없다() {
        Member requester = member(1L, MemberRoleType.ADMIN);
        given(loadMemberPort.findById(1L)).willReturn(Optional.of(requester));

        assertThatThrownBy(() -> sut.updateRole(UpdateMemberRoleCommand.of(1L, 1L, MemberRoleType.NORMAL)))
            .isInstanceOf(MemberDomainException.class)
            .extracting("baseCode")
            .isEqualTo(MemberErrorCode.MEMBER_ROLE_SELF_DEMOTION_DENIED);

        then(saveMemberPort).should(never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("마지막 ADMIN을 NORMAL로 강등할 수 없다")
    void 마지막_ADMIN은_강등할_수_없다() {
        Member requester = member(1L, MemberRoleType.ADMIN);
        Member target = member(2L, MemberRoleType.ADMIN);
        given(loadMemberPort.findById(1L)).willReturn(Optional.of(requester));
        given(loadMemberPort.findById(2L)).willReturn(Optional.of(target));
        given(loadMemberPort.countByRoleType(MemberRoleType.ADMIN)).willReturn(1L);

        assertThatThrownBy(() -> sut.updateRole(UpdateMemberRoleCommand.of(1L, 2L, MemberRoleType.NORMAL)))
            .isInstanceOf(MemberDomainException.class)
            .extracting("baseCode")
            .isEqualTo(MemberErrorCode.LAST_ADMIN_ROLE_CHANGE_DENIED);

        then(saveMemberPort).should(never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Member member(Long id, MemberRoleType roleType) {
        Member member = Member.create("홍길동", "길동" + id, "test" + id + "@example.com", 1L, null);
        ReflectionTestUtils.setField(member, "id", id);
        member.changeRole(roleType);
        return member;
    }
}
