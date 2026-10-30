package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.member.application.port.in.command.dto.MemberBlockCommand;
import com.umc.product.member.application.port.in.command.dto.MemberBlockInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.out.DeleteMemberBlockPort;
import com.umc.product.member.application.port.out.LoadMemberBlockPort;
import com.umc.product.member.application.port.out.SaveMemberBlockPort;
import com.umc.product.member.domain.MemberBlock;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberBlockCommandService")
class MemberBlockCommandServiceTest {

    private static final Long BLOCKER_ID = 10L;
    private static final Long BLOCKED_ID = 20L;

    @Mock
    GetMemberUseCase getMemberUseCase;

    @Mock
    LoadMemberBlockPort loadMemberBlockPort;

    @Mock
    SaveMemberBlockPort saveMemberBlockPort;

    @Mock
    DeleteMemberBlockPort deleteMemberBlockPort;

    MemberBlockCommandService sut;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        sut = new MemberBlockCommandService(
            getMemberUseCase, loadMemberBlockPort, saveMemberBlockPort, deleteMemberBlockPort
        );
    }

    @Test
    @DisplayName("block은 대상이 존재하고 미차단 상태면 새 차단을 저장한다")
    void block_신규_차단을_저장한다() {
        given(getMemberUseCase.existsById(BLOCKED_ID)).willReturn(true);
        given(loadMemberBlockPort.findByBlockerAndBlocked(BLOCKER_ID, BLOCKED_ID)).willReturn(Optional.empty());
        given(saveMemberBlockPort.save(any(MemberBlock.class))).willAnswer(invocation -> invocation.getArgument(0));

        MemberBlockInfo result = sut.block(new MemberBlockCommand(BLOCKER_ID, BLOCKED_ID));

        assertThat(result.blockerMemberId()).isEqualTo(BLOCKER_ID);
        assertThat(result.blockedMemberId()).isEqualTo(BLOCKED_ID);
        assertThat(result.blocked()).isTrue();
        then(saveMemberBlockPort).should().save(any(MemberBlock.class));
    }

    @Test
    @DisplayName("block은 자기 자신을 차단하려 하면 거절하고 어떤 포트도 호출하지 않는다")
    void block_자기_자신이면_거절한다() {
        assertThatThrownBy(() -> sut.block(new MemberBlockCommand(BLOCKER_ID, BLOCKER_ID)))
            .isInstanceOfSatisfying(MemberDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(MemberErrorCode.MEMBER_BLOCK_SELF_NOT_ALLOWED)
            );
        then(getMemberUseCase).should(never()).existsById(any());
        then(saveMemberBlockPort).should(never()).save(any());
    }

    @Test
    @DisplayName("block은 차단 대상이 존재하지 않으면 MEMBER_NOT_FOUND를 던진다")
    void block_대상이_없으면_거절한다() {
        given(getMemberUseCase.existsById(BLOCKED_ID)).willReturn(false);

        assertThatThrownBy(() -> sut.block(new MemberBlockCommand(BLOCKER_ID, BLOCKED_ID)))
            .isInstanceOfSatisfying(MemberDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            );
        then(saveMemberBlockPort).should(never()).save(any());
    }

    @Test
    @DisplayName("block은 이미 차단한 대상이면 MEMBER_BLOCK_ALREADY_EXISTS를 던진다")
    void block_이미_차단했으면_거절한다() {
        given(getMemberUseCase.existsById(BLOCKED_ID)).willReturn(true);
        given(loadMemberBlockPort.findByBlockerAndBlocked(BLOCKER_ID, BLOCKED_ID))
            .willReturn(Optional.of(MemberBlock.create(BLOCKER_ID, BLOCKED_ID)));

        assertThatThrownBy(() -> sut.block(new MemberBlockCommand(BLOCKER_ID, BLOCKED_ID)))
            .isInstanceOfSatisfying(MemberDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(MemberErrorCode.MEMBER_BLOCK_ALREADY_EXISTS)
            );
        then(saveMemberBlockPort).should(never()).save(any());
    }

    @Test
    @DisplayName("unblock은 차단 관계가 있으면 삭제한다")
    void unblock_차단_관계를_삭제한다() {
        given(loadMemberBlockPort.findByBlockerAndBlocked(BLOCKER_ID, BLOCKED_ID))
            .willReturn(Optional.of(MemberBlock.create(BLOCKER_ID, BLOCKED_ID)));

        MemberBlockInfo result = sut.unblock(new MemberBlockCommand(BLOCKER_ID, BLOCKED_ID));

        assertThat(result.blocked()).isFalse();
        then(deleteMemberBlockPort).should().delete(BLOCKER_ID, BLOCKED_ID);
    }

    @Test
    @DisplayName("unblock은 차단한 적 없으면 MEMBER_BLOCK_NOT_FOUND를 던지고 삭제를 호출하지 않는다")
    void unblock_차단한_적_없으면_거절한다() {
        given(loadMemberBlockPort.findByBlockerAndBlocked(BLOCKER_ID, BLOCKED_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> sut.unblock(new MemberBlockCommand(BLOCKER_ID, BLOCKED_ID)))
            .isInstanceOfSatisfying(MemberDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(MemberErrorCode.MEMBER_BLOCK_NOT_FOUND)
            );
        then(deleteMemberBlockPort).should(never()).delete(any(), any());
    }

    @Test
    @DisplayName("deleteAllInvolving은 해당 회원이 걸린 차단 행을 모두 지운다")
    void deleteAllInvolving_회원이_걸린_차단_행을_모두_지운다() {
        sut.deleteAllInvolving(BLOCKER_ID);

        then(deleteMemberBlockPort).should().deleteAllInvolvingMember(BLOCKER_ID);
    }
}
