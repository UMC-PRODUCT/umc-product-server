package com.umc.product.notification.adapter.in.event;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.umc.product.community.application.event.CommunityThreadMessageCreatedEvent;
import com.umc.product.community.application.port.in.query.thread.GetCommunityThreadTitleUseCase;
import com.umc.product.community.application.port.in.query.thread.ListCommunityThreadMemberStatusUseCase;
import com.umc.product.community.application.port.in.query.thread.dto.ThreadMemberStatusInfo;
import com.umc.product.community.application.port.in.query.thread.message.GetCommunityThreadMessageForRecipientsUseCase;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageInfo;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageMentionInfo;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageRecipientsQuery;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageType;
import com.umc.product.community.domain.event.CommunityThreadInvitedEvent;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.notification.application.port.in.RequestFcmNotificationUseCase;
import com.umc.product.notification.application.port.in.dto.RequestFcmNotificationCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * community.thread.message.mentioned은 이 리스너가 소비하지 않는다 — 이 이벤트가 담고 있는
 * 메시지를 다시 조회하면 멘션 정보를 그대로 얻을 수 있어 별도 처리 없이 음소거 우회를 판단한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityAlarmEventListener {

    private static final int TITLE_MAX_LENGTH = 25;
    private static final int BODY_MAX_LENGTH = 40;

    private final ListCommunityThreadMemberStatusUseCase listMemberStatusUseCase;
    private final GetCommunityThreadMessageForRecipientsUseCase getMessageForRecipientsUseCase;
    private final GetCommunityThreadTitleUseCase getThreadTitleUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final RequestFcmNotificationUseCase requestFcmNotificationUseCase;

    @EventListener
    public void handle(CommunityThreadMessageCreatedEvent event) {
        Long threadId = event.threadId();
        Long messageId = event.messageId();
        Long senderMemberId = event.senderMemberId();

        List<ThreadMemberStatusInfo> memberStatuses = listMemberStatusUseCase.listMemberStatus(threadId);
        List<Long> candidateIds = memberStatuses.stream()
            .filter(ThreadMemberStatusInfo::isActive)
            .map(ThreadMemberStatusInfo::memberId)
            .filter(memberId -> !memberId.equals(senderMemberId))
            .toList();
        if (candidateIds.isEmpty()) {
            return;
        }

        Map<Long, CommunityThreadMessageInfo> infoByRecipient = getMessageForRecipientsUseCase
            .getMessageForRecipients(new CommunityThreadMessageRecipientsQuery(threadId, messageId, candidateIds));
        if (infoByRecipient.isEmpty()) {
            log.info("Community 알림 대상 메시지 조회 결과 없음: threadId={}, messageId={}", threadId, messageId);
            return;
        }

        CommunityThreadMessageInfo sample = infoByRecipient.values().iterator().next();
        if (sample.type() == CommunityThreadMessageType.SYSTEM) {
            return;
        }

        Set<Long> mentionedMemberIds = sample.mentions().stream()
            .map(CommunityThreadMessageMentionInfo::memberId)
            .collect(Collectors.toSet());
        Set<Long> mutedMemberIds = memberStatuses.stream()
            .filter(ThreadMemberStatusInfo::isMuted)
            .map(ThreadMemberStatusInfo::memberId)
            .collect(Collectors.toSet());

        // 음소거한 멤버는 제외하되, 멘션당한 멤버는 음소거를 무시하고 그대로 포함한다.
        Set<Long> finalTargetIds = infoByRecipient.keySet().stream()
            .filter(memberId -> !mutedMemberIds.contains(memberId) || mentionedMemberIds.contains(memberId))
            .collect(Collectors.toSet());
        if (finalTargetIds.isEmpty()) {
            log.info("Community 알림 대상이 전부 음소거됨: threadId={}, messageId={}", threadId, messageId);
            return;
        }

        String threadTitle = getThreadTitleUseCase.getThreadTitle(threadId);
        requestFcmNotificationUseCase.request(RequestFcmNotificationCommand.builder()
            .requesterMemberId(senderMemberId)
            .memberIds(List.copyOf(finalTargetIds))
            .title(title(threadTitle, sample))
            .body(body(sample))
            .build());

        log.info("Community 메시지 알림 요청: threadId={}, messageId={}, targetCount={}",
            threadId, messageId, finalTargetIds.size());
    }

    @EventListener
    public void handle(CommunityThreadInvitedEvent event) {
        List<Long> invitedMemberIds = event.invitedMemberIds();
        if (invitedMemberIds.isEmpty()) {
            return;
        }

        String inviterName = getMemberUseCase.getById(event.inviterMemberId()).name();
        String threadTitle = getThreadTitleUseCase.getThreadTitle(event.threadId());

        requestFcmNotificationUseCase.request(RequestFcmNotificationCommand.builder()
            .requesterMemberId(event.inviterMemberId())
            .memberIds(invitedMemberIds)
            .title(inviteTitle(threadTitle))
            .body(inviteBody(inviterName))
            .build());

        log.info("Community 초대 알림 요청: threadId={}, inviterMemberId={}, targetCount={}",
            event.threadId(), event.inviterMemberId(), invitedMemberIds.size());
    }

    private String inviteTitle(String threadTitle) {
        return StringUtils.abbreviate(threadTitle, TITLE_MAX_LENGTH);
    }

    // 수락 대기가 아니라 초대 즉시 멤버가 되는 구조라, 문구도 이미 참여된 사실을 안내한다.
    private String inviteBody(String inviterName) {
        return StringUtils.abbreviate(inviterName + "님이 스레드에 초대해 참여했어요", BODY_MAX_LENGTH);
    }

    private String title(String threadTitle, CommunityThreadMessageInfo info) {
        return StringUtils.abbreviate(threadTitle + " " + info.senderName(), TITLE_MAX_LENGTH);
    }

    private String body(CommunityThreadMessageInfo info) {
        String content = info.content() != null && !info.content().isBlank()
            ? info.content()
            : fallbackBody(info.type());
        return StringUtils.abbreviate(content, BODY_MAX_LENGTH);
    }

    private String fallbackBody(CommunityThreadMessageType type) {
        return type == CommunityThreadMessageType.IMAGE
            ? "사진을 보냈습니다"
            : "새 메시지가 도착했습니다";
    }
}
