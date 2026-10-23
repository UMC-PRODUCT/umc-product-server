package com.umc.product.notification.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.community.application.event.CommunityThreadMessageCreatedEvent;
import com.umc.product.community.application.port.in.query.thread.GetCommunityThreadTitleUseCase;
import com.umc.product.community.application.port.in.query.thread.ListCommunityThreadMemberStatusUseCase;
import com.umc.product.community.application.port.in.query.thread.dto.ThreadMemberStatusInfo;
import com.umc.product.community.application.port.in.query.thread.message.GetCommunityThreadMessageForRecipientsUseCase;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageInfo;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageMentionInfo;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageRecipientsQuery;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageStatus;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageType;
import com.umc.product.notification.application.port.in.RequestFcmNotificationUseCase;
import com.umc.product.notification.application.port.in.dto.FcmNotificationRequestInfo;
import com.umc.product.notification.application.port.in.dto.RequestFcmNotificationCommand;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityAlarmEventListener")
class CommunityAlarmEventListenerTest {

    private static final Long THREAD_ID = 11L;
    private static final Long MESSAGE_ID = 900L;
    private static final Long SENDER_ID = 10L;
    private static final Long RECIPIENT_ID = 20L;
    private static final Long MUTED_ID = 30L;
    private static final Long LEFT_ID = 40L;
    private static final String THREAD_TITLE = "테스트방";
    private static final Instant NOW = Instant.parse("2026-07-18T00:00:00Z");

    @Mock
    ListCommunityThreadMemberStatusUseCase listMemberStatusUseCase;
    @Mock
    GetCommunityThreadMessageForRecipientsUseCase getMessageForRecipientsUseCase;
    @Mock
    GetCommunityThreadTitleUseCase getThreadTitleUseCase;
    @Mock
    RequestFcmNotificationUseCase requestFcmNotificationUseCase;

    @InjectMocks
    CommunityAlarmEventListener sut;

    @Test
    @DisplayName("활성 멤버 중 발신자와 탈퇴한 멤버는 제외하고, 음소거하지 않은 멤버에게 알림을 요청한다")
    void handle_notifiesActiveMembersExcludingSenderMutedAndLeft() {
        given(listMemberStatusUseCase.listMemberStatus(THREAD_ID)).willReturn(List.of(
            status(SENDER_ID, true, false),
            status(RECIPIENT_ID, true, false),
            status(MUTED_ID, true, true),
            status(LEFT_ID, false, false)
        ));
        given(getMessageForRecipientsUseCase.getMessageForRecipients(
            new CommunityThreadMessageRecipientsQuery(THREAD_ID, MESSAGE_ID, List.of(RECIPIENT_ID, MUTED_ID))
        )).willReturn(Map.of(
            RECIPIENT_ID, message("홍길동", "새 메시지", CommunityThreadMessageType.TEXT),
            MUTED_ID, message("홍길동", "새 메시지", CommunityThreadMessageType.TEXT)
        ));
        given(getThreadTitleUseCase.getThreadTitle(THREAD_ID)).willReturn(THREAD_TITLE);
        given(requestFcmNotificationUseCase.request(any()))
            .willReturn(FcmNotificationRequestInfo.of(UUID.randomUUID(), NOW));

        sut.handle(CommunityThreadMessageCreatedEvent.of(THREAD_ID, MESSAGE_ID, SENDER_ID));

        ArgumentCaptor<RequestFcmNotificationCommand> captor =
            ArgumentCaptor.forClass(RequestFcmNotificationCommand.class);
        then(requestFcmNotificationUseCase).should().request(captor.capture());
        RequestFcmNotificationCommand command = captor.getValue();
        assertThat(command.memberIds()).containsExactly(RECIPIENT_ID);
        assertThat(command.title()).isEqualTo("테스트방 홍길동");
        assertThat(command.body()).isEqualTo("새 메시지");
    }

    @Test
    @DisplayName("음소거했더라도 멘션당한 멤버는 알림 대상에 포함된다")
    void handle_mutedButMentionedMember_isIncluded() {
        given(listMemberStatusUseCase.listMemberStatus(THREAD_ID)).willReturn(List.of(
            status(SENDER_ID, true, false),
            status(MUTED_ID, true, true)
        ));
        given(getMessageForRecipientsUseCase.getMessageForRecipients(
            new CommunityThreadMessageRecipientsQuery(THREAD_ID, MESSAGE_ID, List.of(MUTED_ID))
        )).willReturn(Map.of(
            MUTED_ID, messageWithMentions("홍길동", "@member 안녕", CommunityThreadMessageType.TEXT, List.of(MUTED_ID))
        ));
        given(getThreadTitleUseCase.getThreadTitle(THREAD_ID)).willReturn(THREAD_TITLE);
        given(requestFcmNotificationUseCase.request(any()))
            .willReturn(FcmNotificationRequestInfo.of(UUID.randomUUID(), NOW));

        sut.handle(CommunityThreadMessageCreatedEvent.of(THREAD_ID, MESSAGE_ID, SENDER_ID));

        ArgumentCaptor<RequestFcmNotificationCommand> captor =
            ArgumentCaptor.forClass(RequestFcmNotificationCommand.class);
        then(requestFcmNotificationUseCase).should().request(captor.capture());
        assertThat(captor.getValue().memberIds()).containsExactly(MUTED_ID);
    }

    @Test
    @DisplayName("발신자를 제외하면 활성 후보가 없으면 이후 조회 없이 종료한다")
    void handle_noActiveCandidates_doesNothing() {
        given(listMemberStatusUseCase.listMemberStatus(THREAD_ID)).willReturn(List.of(
            status(SENDER_ID, true, false)
        ));

        sut.handle(CommunityThreadMessageCreatedEvent.of(THREAD_ID, MESSAGE_ID, SENDER_ID));

        then(getMessageForRecipientsUseCase).shouldHaveNoInteractions();
        then(getThreadTitleUseCase).shouldHaveNoInteractions();
        then(requestFcmNotificationUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("후보 전원이 음소거(비멘션)면 알림 요청을 생략한다")
    void handle_allCandidatesMuted_skipsNotification() {
        given(listMemberStatusUseCase.listMemberStatus(THREAD_ID)).willReturn(List.of(
            status(SENDER_ID, true, false),
            status(MUTED_ID, true, true)
        ));
        given(getMessageForRecipientsUseCase.getMessageForRecipients(
            new CommunityThreadMessageRecipientsQuery(THREAD_ID, MESSAGE_ID, List.of(MUTED_ID))
        )).willReturn(Map.of(MUTED_ID, message("홍길동", "새 메시지", CommunityThreadMessageType.TEXT)));

        sut.handle(CommunityThreadMessageCreatedEvent.of(THREAD_ID, MESSAGE_ID, SENDER_ID));

        then(requestFcmNotificationUseCase).should(never()).request(any());
    }

    @Test
    @DisplayName("메시지 조회 결과가 없으면 알림 요청을 생략한다")
    void handle_emptyMessageInfo_doesNothing() {
        given(listMemberStatusUseCase.listMemberStatus(THREAD_ID)).willReturn(List.of(
            status(SENDER_ID, true, false),
            status(RECIPIENT_ID, true, false)
        ));
        given(getMessageForRecipientsUseCase.getMessageForRecipients(
            new CommunityThreadMessageRecipientsQuery(THREAD_ID, MESSAGE_ID, List.of(RECIPIENT_ID))
        )).willReturn(Map.of());

        sut.handle(CommunityThreadMessageCreatedEvent.of(THREAD_ID, MESSAGE_ID, SENDER_ID));

        then(requestFcmNotificationUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("SYSTEM 타입 메시지는 알림을 요청하지 않는다")
    void handle_systemTypeMessage_skipsNotification() {
        given(listMemberStatusUseCase.listMemberStatus(THREAD_ID)).willReturn(List.of(
            status(SENDER_ID, true, false),
            status(RECIPIENT_ID, true, false)
        ));
        given(getMessageForRecipientsUseCase.getMessageForRecipients(
            new CommunityThreadMessageRecipientsQuery(THREAD_ID, MESSAGE_ID, List.of(RECIPIENT_ID))
        )).willReturn(Map.of(RECIPIENT_ID, message(null, null, CommunityThreadMessageType.SYSTEM)));

        sut.handle(CommunityThreadMessageCreatedEvent.of(THREAD_ID, MESSAGE_ID, SENDER_ID));

        then(requestFcmNotificationUseCase).should(never()).request(any());
    }

    @Test
    @DisplayName("본문이 비어있는 IMAGE 메시지는 대체 문구로 알림 본문을 채운다")
    void handle_blankImageContent_usesFallbackBody() {
        given(listMemberStatusUseCase.listMemberStatus(THREAD_ID)).willReturn(List.of(
            status(SENDER_ID, true, false),
            status(RECIPIENT_ID, true, false)
        ));
        given(getMessageForRecipientsUseCase.getMessageForRecipients(
            new CommunityThreadMessageRecipientsQuery(THREAD_ID, MESSAGE_ID, List.of(RECIPIENT_ID))
        )).willReturn(Map.of(RECIPIENT_ID, message("홍길동", "", CommunityThreadMessageType.IMAGE)));
        given(getThreadTitleUseCase.getThreadTitle(THREAD_ID)).willReturn(THREAD_TITLE);
        given(requestFcmNotificationUseCase.request(any()))
            .willReturn(FcmNotificationRequestInfo.of(UUID.randomUUID(), NOW));

        sut.handle(CommunityThreadMessageCreatedEvent.of(THREAD_ID, MESSAGE_ID, SENDER_ID));

        ArgumentCaptor<RequestFcmNotificationCommand> captor =
            ArgumentCaptor.forClass(RequestFcmNotificationCommand.class);
        then(requestFcmNotificationUseCase).should().request(captor.capture());
        assertThat(captor.getValue().body()).isEqualTo("사진을 보냈습니다");
    }

    @Test
    @DisplayName("제목은 25자, 본문은 40자를 넘으면 잘라낸다")
    void handle_longTitleAndBody_areAbbreviated() {
        String longContent = "가".repeat(50);
        given(listMemberStatusUseCase.listMemberStatus(THREAD_ID)).willReturn(List.of(
            status(SENDER_ID, true, false),
            status(RECIPIENT_ID, true, false)
        ));
        given(getMessageForRecipientsUseCase.getMessageForRecipients(
            new CommunityThreadMessageRecipientsQuery(THREAD_ID, MESSAGE_ID, List.of(RECIPIENT_ID))
        )).willReturn(Map.of(RECIPIENT_ID, message("홍길동", longContent, CommunityThreadMessageType.TEXT)));
        given(getThreadTitleUseCase.getThreadTitle(THREAD_ID)).willReturn("긴".repeat(30));
        given(requestFcmNotificationUseCase.request(any()))
            .willReturn(FcmNotificationRequestInfo.of(UUID.randomUUID(), NOW));

        sut.handle(CommunityThreadMessageCreatedEvent.of(THREAD_ID, MESSAGE_ID, SENDER_ID));

        ArgumentCaptor<RequestFcmNotificationCommand> captor =
            ArgumentCaptor.forClass(RequestFcmNotificationCommand.class);
        then(requestFcmNotificationUseCase).should().request(captor.capture());
        assertThat(captor.getValue().title()).hasSize(25);
        assertThat(captor.getValue().body()).hasSize(40);
    }

    @Test
    @DisplayName("멘션이 포함된 메시지라도 알림 요청은 한 번만 나간다")
    void handle_mentionedMessage_sendsExactlyOneRequest() {
        given(listMemberStatusUseCase.listMemberStatus(THREAD_ID)).willReturn(List.of(
            status(SENDER_ID, true, false),
            status(RECIPIENT_ID, true, false)
        ));
        given(getMessageForRecipientsUseCase.getMessageForRecipients(
            new CommunityThreadMessageRecipientsQuery(THREAD_ID, MESSAGE_ID, List.of(RECIPIENT_ID))
        )).willReturn(Map.of(
            RECIPIENT_ID, messageWithMentions("홍길동", "@member 안녕", CommunityThreadMessageType.TEXT, List.of(RECIPIENT_ID))
        ));
        given(getThreadTitleUseCase.getThreadTitle(THREAD_ID)).willReturn(THREAD_TITLE);
        given(requestFcmNotificationUseCase.request(any()))
            .willReturn(FcmNotificationRequestInfo.of(UUID.randomUUID(), NOW));

        sut.handle(CommunityThreadMessageCreatedEvent.of(THREAD_ID, MESSAGE_ID, SENDER_ID));

        then(requestFcmNotificationUseCase).should(org.mockito.Mockito.times(1)).request(any());
    }

    private ThreadMemberStatusInfo status(Long memberId, boolean isActive, boolean muted) {
        return new ThreadMemberStatusInfo(memberId, isActive, muted);
    }

    private CommunityThreadMessageInfo message(String senderName, String content, CommunityThreadMessageType type) {
        return messageWithMentions(senderName, content, type, List.of());
    }

    private CommunityThreadMessageInfo messageWithMentions(
        String senderName, String content, CommunityThreadMessageType type, List<Long> mentionedMemberIds
    ) {
        List<CommunityThreadMessageMentionInfo> mentions = mentionedMemberIds.stream()
            .map(memberId -> new CommunityThreadMessageMentionInfo(memberId, "멘션멤버"))
            .toList();
        return new CommunityThreadMessageInfo(
            MESSAGE_ID,
            THREAD_ID,
            SENDER_ID,
            senderName,
            content,
            type,
            CommunityThreadMessageStatus.SENT,
            List.of(),
            mentions,
            null,
            List.of(),
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            NOW,
            null,
            null
        );
    }
}
