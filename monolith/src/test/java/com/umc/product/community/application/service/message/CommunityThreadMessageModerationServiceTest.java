package com.umc.product.community.application.service.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.community.application.port.in.command.thread.message.TombstoneCommunityThreadMessageUseCase;
import com.umc.product.community.application.port.in.command.thread.message.dto.CommunityThreadMessageRemovalResult;
import com.umc.product.community.application.port.in.command.thread.message.dto.TombstoneCommunityThreadMessageCommand;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageMutationInfo;
import com.umc.product.community.application.port.out.thread.LoadCommunityThreadMemberPort;
import com.umc.product.community.application.port.out.thread.LoadCommunityThreadPort;
import com.umc.product.community.domain.CommunityThread;
import com.umc.product.community.domain.CommunityThreadMember;
import com.umc.product.community.domain.enums.CommunityThreadCategory;
import com.umc.product.community.domain.enums.CommunityThreadMemberRole;

@ExtendWith(MockitoExtension.class)
@DisplayName("커뮤니티 스레드 메시지 운영 삭제 서비스")
class CommunityThreadMessageModerationServiceTest {

    private static final Long THREAD_ID = 10L;
    private static final Long MESSAGE_ID = 20L;
    private static final Long OWNER_ID = 100L;
    private static final Long ADMIN_ID = 200L;
    private static final Long MEMBER_ID = 300L;
    private static final Instant NOW = Instant.parse("2026-10-29T00:00:00Z");

    @Mock
    LoadCommunityThreadPort loadThreadPort;

    @Mock
    LoadCommunityThreadMemberPort loadThreadMemberPort;

    @Mock
    TombstoneCommunityThreadMessageUseCase tombstoneCommunityThreadMessageUseCase;

    @InjectMocks
    CommunityThreadMessageModerationService sut;

    @Test
    @DisplayName("활성 방장 권한으로 메시지를 삭제하고 DELETED를 돌려준다")
    void removeByOperator_방장_권한으로_삭제한다() {
        // given
        given(loadThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(thread()));
        given(loadThreadMemberPort.listByThreadId(THREAD_ID)).willReturn(List.of(
            active(MEMBER_ID, CommunityThreadMemberRole.MEMBER),
            active(ADMIN_ID, CommunityThreadMemberRole.ADMIN),
            active(OWNER_ID, CommunityThreadMemberRole.OWNER)
        ));
        given(tombstoneCommunityThreadMessageUseCase.tombstone(any())).willReturn(mutation(false));

        // when
        CommunityThreadMessageRemovalResult result = sut.removeByOperator(THREAD_ID, MESSAGE_ID);

        // then
        assertThat(result).isEqualTo(CommunityThreadMessageRemovalResult.DELETED);
        then(tombstoneCommunityThreadMessageUseCase).should()
            .tombstone(new TombstoneCommunityThreadMessageCommand(THREAD_ID, MESSAGE_ID, OWNER_ID));
    }

    @Test
    @DisplayName("활성 방장이 없으면 활성 ADMIN 권한으로 삭제한다")
    void removeByOperator_방장이_없으면_ADMIN으로_삭제한다() {
        // given
        CommunityThreadMember leftOwner = active(OWNER_ID, CommunityThreadMemberRole.OWNER);
        leftOwner.leave(NOW.plusSeconds(1));
        given(loadThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(thread()));
        given(loadThreadMemberPort.listByThreadId(THREAD_ID)).willReturn(List.of(
            leftOwner,
            active(MEMBER_ID, CommunityThreadMemberRole.MEMBER),
            active(ADMIN_ID, CommunityThreadMemberRole.ADMIN)
        ));
        given(tombstoneCommunityThreadMessageUseCase.tombstone(any())).willReturn(mutation(false));

        // when
        CommunityThreadMessageRemovalResult result = sut.removeByOperator(THREAD_ID, MESSAGE_ID);

        // then
        assertThat(result).isEqualTo(CommunityThreadMessageRemovalResult.DELETED);
        then(tombstoneCommunityThreadMessageUseCase).should()
            .tombstone(new TombstoneCommunityThreadMessageCommand(THREAD_ID, MESSAGE_ID, ADMIN_ID));
    }

    @Test
    @DisplayName("이미 삭제된 메시지면 ALREADY_DELETED를 돌려준다")
    void removeByOperator_이미_삭제된_메시지는_ALREADY_DELETED() {
        // given
        given(loadThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(thread()));
        given(loadThreadMemberPort.listByThreadId(THREAD_ID))
            .willReturn(List.of(active(OWNER_ID, CommunityThreadMemberRole.OWNER)));
        given(tombstoneCommunityThreadMessageUseCase.tombstone(any())).willReturn(mutation(true));

        // when & then
        assertThat(sut.removeByOperator(THREAD_ID, MESSAGE_ID))
            .isEqualTo(CommunityThreadMessageRemovalResult.ALREADY_DELETED);
    }

    @Test
    @DisplayName("스레드가 없으면 삭제를 호출하지 않고 NOT_FOUND를 돌려준다")
    void removeByOperator_스레드가_없으면_NOT_FOUND() {
        // given
        given(loadThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.empty());

        // when & then
        assertThat(sut.removeByOperator(THREAD_ID, MESSAGE_ID))
            .isEqualTo(CommunityThreadMessageRemovalResult.NOT_FOUND);
        then(loadThreadMemberPort).shouldHaveNoInteractions();
        then(tombstoneCommunityThreadMessageUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("삭제된 스레드면 삭제를 호출하지 않고 NOT_FOUND를 돌려준다")
    void removeByOperator_삭제된_스레드는_NOT_FOUND() {
        // given
        CommunityThread deleted = thread();
        deleted.delete(NOW);
        given(loadThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(deleted));

        // when & then
        assertThat(sut.removeByOperator(THREAD_ID, MESSAGE_ID))
            .isEqualTo(CommunityThreadMessageRemovalResult.NOT_FOUND);
        then(loadThreadMemberPort).shouldHaveNoInteractions();
        then(tombstoneCommunityThreadMessageUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("활성 방장과 ADMIN이 모두 없으면 예외를 던져 신고 처리 전체를 롤백시킨다")
    void removeByOperator_대행자가_없으면_예외() {
        // given
        given(loadThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(thread()));
        given(loadThreadMemberPort.listByThreadId(THREAD_ID))
            .willReturn(List.of(active(MEMBER_ID, CommunityThreadMemberRole.MEMBER)));

        // when & then
        assertThatThrownBy(() -> sut.removeByOperator(THREAD_ID, MESSAGE_ID))
            .isInstanceOf(IllegalStateException.class);
        then(tombstoneCommunityThreadMessageUseCase).shouldHaveNoInteractions();
    }

    private CommunityThread thread() {
        CommunityThread thread = CommunityThread.create(
            1L, "스레드", null, CommunityThreadCategory.FREE, "💬", OWNER_ID, NOW
        );
        ReflectionTestUtils.setField(thread, "id", THREAD_ID);
        return thread;
    }

    private CommunityThreadMember active(Long memberId, CommunityThreadMemberRole role) {
        CommunityThreadMember member = role == CommunityThreadMemberRole.OWNER
            ? CommunityThreadMember.createOwner(THREAD_ID, memberId, NOW)
            : CommunityThreadMember.createMember(THREAD_ID, memberId, NOW);
        if (role == CommunityThreadMemberRole.ADMIN) {
            member.changeRole(CommunityThreadMemberRole.ADMIN);
        }
        return member;
    }

    private CommunityThreadMessageMutationInfo mutation(boolean deduplicated) {
        return new CommunityThreadMessageMutationInfo(null, deduplicated);
    }
}
