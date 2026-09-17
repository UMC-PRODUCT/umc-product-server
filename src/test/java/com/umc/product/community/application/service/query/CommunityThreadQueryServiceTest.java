package com.umc.product.community.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

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

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.community.application.port.in.query.thread.dto.ThreadMemberStatusInfo;
import com.umc.product.community.application.port.out.thread.CommunityThreadQueryPort;
import com.umc.product.community.application.port.out.thread.LoadCommunityThreadMemberPort;
import com.umc.product.community.application.port.out.thread.LoadCommunityThreadPort;
import com.umc.product.community.domain.CommunityThread;
import com.umc.product.community.domain.CommunityThreadMember;
import com.umc.product.community.domain.CommunityThreadProperties;
import com.umc.product.community.domain.enums.CommunityThreadCategory;
import com.umc.product.community.domain.exception.CommunityDomainException;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.SearchMemberInvitationUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityThreadQueryService")
class CommunityThreadQueryServiceTest {

    private static final Long THREAD_ID = 11L;
    private static final Long OWNER_ID = 10L;
    private static final Long MUTED_ID = 20L;
    private static final Long LEFT_ID = 30L;
    private static final Instant NOW = Instant.parse("2026-07-18T00:00:00Z");

    @Mock
    CommunityThreadQueryPort threadQueryPort;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    GetGisuUseCase getGisuUseCase;
    @Mock
    SearchMemberInvitationUseCase searchInvitationUseCase;
    @Mock
    CommunityThreadProperties threadProperties;
    @Mock
    LoadCommunityThreadMemberPort loadThreadMemberPort;
    @Mock
    LoadCommunityThreadPort loadThreadPort;

    @InjectMocks
    CommunityThreadQueryService sut;

    @Test
    @DisplayName("멤버 상태 조회는 스레드의 모든 멤버에 대해 isActive·isMuted를 그대로 반환한다")
    void listMemberStatus_returnsStatusForAllMembers() {
        CommunityThreadMember owner = CommunityThreadMember.createOwner(THREAD_ID, OWNER_ID, NOW);
        CommunityThreadMember muted = CommunityThreadMember.createMember(THREAD_ID, MUTED_ID, NOW);
        muted.mute();
        CommunityThreadMember left = CommunityThreadMember.createMember(THREAD_ID, LEFT_ID, NOW);
        left.leave(NOW.plusSeconds(1));
        given(loadThreadMemberPort.listByThreadId(THREAD_ID)).willReturn(List.of(owner, muted, left));

        List<ThreadMemberStatusInfo> result = sut.listMemberStatus(THREAD_ID);

        assertThat(result).hasSize(3);
        assertThat(result).anySatisfy(info -> {
            assertThat(info.memberId()).isEqualTo(OWNER_ID);
            assertThat(info.isActive()).isTrue();
            assertThat(info.isMuted()).isFalse();
        });
        assertThat(result).anySatisfy(info -> {
            assertThat(info.memberId()).isEqualTo(MUTED_ID);
            assertThat(info.isActive()).isTrue();
            assertThat(info.isMuted()).isTrue();
        });
        assertThat(result).anySatisfy(info -> {
            assertThat(info.memberId()).isEqualTo(LEFT_ID);
            assertThat(info.isActive()).isFalse();
        });
    }

    @Test
    @DisplayName("멤버가 없는 스레드는 빈 리스트를 반환한다")
    void listMemberStatus_noMembers_returnsEmpty() {
        given(loadThreadMemberPort.listByThreadId(THREAD_ID)).willReturn(List.of());

        List<ThreadMemberStatusInfo> result = sut.listMemberStatus(THREAD_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("스레드 제목 조회는 존재하는 스레드의 title을 그대로 반환한다")
    void getThreadTitle_returnsTitle() {
        CommunityThread thread = CommunityThread.create(
            101L, "스레드 제목", null, CommunityThreadCategory.FREE, "💬", OWNER_ID, NOW
        );
        ReflectionTestUtils.setField(thread, "id", THREAD_ID);
        given(loadThreadPort.findById(THREAD_ID)).willReturn(Optional.of(thread));

        String result = sut.getThreadTitle(THREAD_ID);

        assertThat(result).isEqualTo("스레드 제목");
    }

    @Test
    @DisplayName("존재하지 않는 스레드는 THREAD_NOT_FOUND로 거절한다")
    void getThreadTitle_missingThread_throws() {
        given(loadThreadPort.findById(THREAD_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> sut.getThreadTitle(THREAD_ID))
            .isInstanceOf(CommunityDomainException.class)
            .extracting(error -> ((CommunityDomainException) error).getBaseCode())
            .isEqualTo(CommunityErrorCode.THREAD_NOT_FOUND);
    }
}
