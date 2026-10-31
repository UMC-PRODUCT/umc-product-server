package com.umc.product.community.application.service.message;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.community.application.port.in.command.thread.message.ModerateCommunityThreadMessageUseCase;
import com.umc.product.community.application.port.in.command.thread.message.TombstoneCommunityThreadMessageUseCase;
import com.umc.product.community.application.port.in.command.thread.message.dto.CommunityThreadMessageRemovalResult;
import com.umc.product.community.application.port.in.command.thread.message.dto.TombstoneCommunityThreadMessageCommand;
import com.umc.product.community.application.port.out.thread.LoadCommunityThreadMemberPort;
import com.umc.product.community.application.port.out.thread.LoadCommunityThreadPort;
import com.umc.product.community.domain.CommunityThread;
import com.umc.product.community.domain.CommunityThreadMember;
import com.umc.product.community.domain.enums.CommunityThreadMemberRole;

import lombok.RequiredArgsConstructor;

/**
 * 운영 조치로 스레드 메시지를 삭제합니다.
 * <p>
 * 운영 조치를 방장 권한으로 대신 실행하며, 실제 조치자는 신고 조치 이력(report_action)에 남는다.
 * 기존 삭제 흐름(실시간 삭제 이벤트, 마지막 메시지 미리보기 갱신)을 그대로 재사용합니다.
 * <p>
 * 다른 빈의 {@code @Transactional} 메서드에서 예외가 나면 catch해도 트랜잭션이 rollback-only가 되므로, 예외를 NOT_FOUND로
 * 바꾸지 않고 스레드 존재 여부를 호출 전에 확인합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CommunityThreadMessageModerationService implements ModerateCommunityThreadMessageUseCase {

    private final LoadCommunityThreadPort loadThreadPort;
    private final LoadCommunityThreadMemberPort loadThreadMemberPort;
    private final TombstoneCommunityThreadMessageUseCase tombstoneCommunityThreadMessageUseCase;

    @Override
    public CommunityThreadMessageRemovalResult removeByOperator(Long threadId, Long messageId) {
        Optional<CommunityThread> thread = loadThreadPort.findByIdForUpdate(threadId);
        if (thread.isEmpty() || thread.get().isDeleted()) {
            return CommunityThreadMessageRemovalResult.NOT_FOUND;
        }

        Long actorMemberId = findActor(threadId).getMemberId();
        boolean deduplicated = tombstoneCommunityThreadMessageUseCase.tombstone(
            new TombstoneCommunityThreadMessageCommand(threadId, messageId, actorMemberId)
        ).deduplicated();
        return deduplicated
            ? CommunityThreadMessageRemovalResult.ALREADY_DELETED
            : CommunityThreadMessageRemovalResult.DELETED;
    }

    /** 활성 방장(OWNER)을 우선하고, 없으면 활성 ADMIN을 고릅니다. 둘 다 없으면 신고 처리 전체를 롤백합니다. */
    private CommunityThreadMember findActor(Long threadId) {
        List<CommunityThreadMember> members = loadThreadMemberPort.listByThreadId(threadId);
        return findActiveByRole(members, CommunityThreadMemberRole.OWNER)
            .or(() -> findActiveByRole(members, CommunityThreadMemberRole.ADMIN))
            .orElseThrow(() -> new IllegalStateException(
                "no active OWNER or ADMIN to act for thread " + threadId));
    }

    private Optional<CommunityThreadMember> findActiveByRole(
        List<CommunityThreadMember> members, CommunityThreadMemberRole role
    ) {
        return members.stream()
            .filter(CommunityThreadMember::isActive)
            .filter(member -> member.getRole() == role)
            .findFirst();
    }
}
