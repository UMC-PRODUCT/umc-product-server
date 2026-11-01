package com.umc.product.community.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.umc.product.community.application.port.out.thread.dto.CommunityThreadListCondition;
import com.umc.product.community.application.port.out.thread.dto.CommunityThreadListRows;
import com.umc.product.community.application.port.out.thread.dto.CommunityThreadQueryRow;
import com.umc.product.community.domain.CommunityThread;
import com.umc.product.community.domain.CommunityThreadMember;
import com.umc.product.community.domain.enums.CommunityThreadCategory;
import com.umc.product.community.domain.enums.CommunityThreadMemberState;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import(CommunityThreadQueryRepository.class)
@DisplayName("CommunityThreadQueryRepository")
class CommunityThreadQueryRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-07-18T00:00:00Z");

    @Autowired
    CommunityThreadQueryRepository sut;

    @Autowired
    CommunityThreadRepository threadRepository;

    @Autowired
    CommunityThreadMemberRepository memberRepository;

    @Test
    @DisplayName("팬아웃 대상은 ACTIVE 멤버만 memberId 순서와 limit에 맞게 조회한다")
    void listActiveMemberIds_활성_멤버만_제한해_조회한다() {
        // given
        CommunityThread thread = threadRepository.save(createThread(110L));
        CommunityThreadMember owner = CommunityThreadMember.createOwner(thread.getId(), 30L, NOW);
        CommunityThreadMember active = CommunityThreadMember.createMember(thread.getId(), 10L, NOW);
        CommunityThreadMember left = CommunityThreadMember.createMember(thread.getId(), 20L, NOW);
        left.leave(NOW.plusSeconds(1));
        memberRepository.saveAll(List.of(owner, active, left));

        // when
        List<Long> memberIds = sut.listActiveMemberIdsByThreadId(thread.getId(), 1);

        // then
        assertThat(memberIds).containsExactly(10L);
    }

    @Test
    @DisplayName("초대 제외 대상은 ACTIVE와 KICKED이고 LEFT는 포함하지 않는다")
    void listInvitationBlockedMemberIds_left를_제외한다() {
        // given
        CommunityThread thread = threadRepository.save(createThread(111L));
        CommunityThreadMember active = CommunityThreadMember.createMember(thread.getId(), 10L, NOW);
        CommunityThreadMember left = CommunityThreadMember.createMember(thread.getId(), 20L, NOW);
        CommunityThreadMember kicked = CommunityThreadMember.createMember(thread.getId(), 30L, NOW);
        left.leave(NOW.plusSeconds(1));
        kicked.kick(NOW.plusSeconds(1));
        memberRepository.saveAll(List.of(active, left, kicked));

        // when
        List<Long> blockedMemberIds = sut.listInvitationBlockedMemberIds(thread.getId());

        // then
        assertThat(blockedMemberIds).containsExactly(10L, 30L);
    }

    @Test
    @DisplayName("강퇴를 확인한 멤버도 초대 제외 대상이다")
    void listInvitationBlockedMemberIds_kickAcknowledged도_포함한다() {
        // given
        CommunityThread thread = threadRepository.save(createThread(112L));
        CommunityThreadMember kickAcknowledged = CommunityThreadMember.createMember(thread.getId(), 40L, NOW);
        kickAcknowledged.kick(NOW.plusSeconds(1));
        kickAcknowledged.acknowledgeKick();
        memberRepository.save(kickAcknowledged);

        // when
        List<Long> blockedMemberIds = sut.listInvitationBlockedMemberIds(thread.getId());

        // then
        assertThat(blockedMemberIds).containsExactly(40L);
    }

    @Test
    @DisplayName("강퇴를 확인하지 않은 스레드는 비활성 상태로 목록에 남고, 확인한 스레드는 목록에서 사라진다")
    void browseThreads_강퇴_확인_여부에_따라_목록_노출이_달라진다() {
        // given
        CommunityThread pendingThread = threadRepository.save(createThread(113L));
        CommunityThreadMember pendingKickMember = CommunityThreadMember.createMember(
            pendingThread.getId(), 50L, NOW
        );
        pendingKickMember.kick(NOW.plusSeconds(1));
        memberRepository.save(pendingKickMember);

        CommunityThread acknowledgedThread = threadRepository.save(createThread(114L));
        CommunityThreadMember acknowledgedKickMember = CommunityThreadMember.createMember(
            acknowledgedThread.getId(), 50L, NOW
        );
        acknowledgedKickMember.kick(NOW.plusSeconds(1));
        acknowledgedKickMember.acknowledgeKick();
        memberRepository.save(acknowledgedKickMember);

        // when
        CommunityThreadListRows rows = sut.browseThreads(
            new CommunityThreadListCondition(50L, null, false, null, 0, 20)
        );

        // then
        List<Long> visibleThreadIds = rows.unpinned().stream()
            .map(CommunityThreadQueryRow::threadId)
            .toList();
        assertThat(visibleThreadIds).contains(pendingThread.getId());
        assertThat(visibleThreadIds).doesNotContain(acknowledgedThread.getId());

        CommunityThreadQueryRow pendingRow = rows.unpinned().stream()
            .filter(row -> row.threadId().equals(pendingThread.getId()))
            .findFirst()
            .orElseThrow();
        assertThat(pendingRow.requesterState()).isEqualTo(CommunityThreadMemberState.KICKED);
    }

    private CommunityThread createThread(Long chatRoomId) {
        return CommunityThread.create(
            chatRoomId,
            "스레드",
            null,
            CommunityThreadCategory.STUDY,
            "📚",
            10L,
            NOW
        );
    }
}
