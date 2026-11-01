package com.umc.product.test.application.service.qa;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.community.application.port.in.command.thread.CreateCommunityThreadUseCase;
import com.umc.product.community.application.port.in.command.thread.dto.CreateCommunityThreadCommand;
import com.umc.product.community.application.port.in.command.thread.message.CreateCommunityThreadMessageUseCase;
import com.umc.product.community.application.port.in.command.thread.message.EditCommunityThreadMessageUseCase;
import com.umc.product.community.application.port.in.command.thread.message.ManageCommunityThreadMessageReactionUseCase;
import com.umc.product.community.application.port.in.command.thread.message.TombstoneCommunityThreadMessageUseCase;
import com.umc.product.community.application.port.in.command.thread.message.UpdateCommunityThreadReadUseCase;
import com.umc.product.community.application.port.in.command.thread.message.dto.ChangeCommunityThreadMessageReactionCommand;
import com.umc.product.community.application.port.in.command.thread.message.dto.CreateCommunityThreadMessageCommand;
import com.umc.product.community.application.port.in.command.thread.message.dto.EditCommunityThreadMessageCommand;
import com.umc.product.community.application.port.in.command.thread.message.dto.TombstoneCommunityThreadMessageCommand;
import com.umc.product.community.application.port.in.command.thread.message.dto.UpdateCommunityThreadReadCommand;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageType;
import com.umc.product.community.domain.enums.CommunityThreadCategory;

import lombok.RequiredArgsConstructor;

@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Transactional(propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class QaChatSeedScenario {

    private final CreateCommunityThreadUseCase createThreadUseCase;
    private final CreateCommunityThreadMessageUseCase createMessageUseCase;
    private final EditCommunityThreadMessageUseCase editMessageUseCase;
    private final TombstoneCommunityThreadMessageUseCase tombstoneMessageUseCase;
    private final ManageCommunityThreadMessageReactionUseCase reactionUseCase;
    private final UpdateCommunityThreadReadUseCase updateReadUseCase;

    public void seed(QaSeedContext context) {
        Participants members = new Participants(
            context.memberId("cau_g11_schoolpresident"),
            context.memberId("cau_g11_web"),
            context.memberId("cau_g11_mobile"),
            context.memberId("cau_g11_design")
        );
        createThread("빈 채팅방", CommunityThreadCategory.FREE, members);
        seedConversation(members);
        seedMessageHistory(members);
        seedMessageFeatures(members);
    }

    private void seedConversation(Participants members) {
        Long threadId = createThread("스터디 대화 · 읽음 확인", CommunityThreadCategory.STUDY, members);
        send(threadId, members.owner(), "welcome", "이번 주 스터디 시간을 정해볼까요?", null);
        send(threadId, members.web(), "question", "화요일 저녁은 어떠세요?", null);
        Long thirdMessageId = send(threadId, members.mobile(), "suggestion", "저는 오후 7시부터 가능합니다.", null);
        Long lastMessageId = send(threadId, members.owner(), "answer", "그럼 화요일 오후 7시에 만나요!", null);

        // 웹은 마지막 한 건을 안 읽고, 모바일은 모두 읽고, 디자인은 대화 전체를 안 읽은 상태다.
        updateReadUseCase.update(new UpdateCommunityThreadReadCommand(threadId, members.web(), thirdMessageId));
        updateReadUseCase.update(new UpdateCommunityThreadReadCommand(threadId, members.mobile(), lastMessageId));
    }

    private void seedMessageHistory(Participants members) {
        Long threadId = createThread("이전 메시지 불러오기", CommunityThreadCategory.FREE, members);
        List<Long> senders = List.of(members.owner(), members.web(), members.mobile());
        // 기본 조회 크기 30개보다 많이 만들어 세 페이지와 마지막 페이지의 끝을 확인한다.
        for (int index = 1; index <= 65; index++) {
            send(threadId, senders.get((index - 1) % senders.size()), "history-" + index,
                "[QA 대화 " + index + "] 이전 메시지 조회와 시간순 정렬을 확인합니다.", null);
        }
    }

    private void seedMessageFeatures(Participants members) {
        Long threadId = createThread("답장 · 리액션 · 수정 · 삭제", CommunityThreadCategory.QNA, members);
        Long originalId = send(threadId, members.owner(), "original", "워크북 제출 방법이 궁금하면 여기에 질문해주세요.", null);
        send(threadId, members.web(), "reply", "제출 후에도 내용을 수정할 수 있나요?", originalId);
        Long editedId = send(threadId, members.mobile(), "edit", "내일 확인하겠습니다.", null);
        editMessageUseCase.edit(new EditCommunityThreadMessageCommand(
            threadId, editedId, members.mobile(), "오늘 저녁에 확인하겠습니다. (수정된 메시지)"));
        Long deletedId = send(threadId, members.web(), "delete", "잘못 보낸 메시지입니다.", null);
        tombstoneMessageUseCase.tombstone(new TombstoneCommunityThreadMessageCommand(
            threadId, deletedId, members.web()));
        for (Long memberId : List.of(members.web(), members.mobile())) {
            reactionUseCase.add(new ChangeCommunityThreadMessageReactionCommand(threadId, originalId, memberId, "👍"));
        }
        send(threadId, members.owner(), "finish", "답장 원문, 리액션 개수와 수정·삭제 표시를 확인해주세요.", null);
    }

    private Long createThread(String title, CommunityThreadCategory category, Participants members) {
        // Chat room만 만들면 앱의 스레드 목록에 나타나지 않아 Community의 공개 생성 경로를 사용한다.
        return createThreadUseCase.create(new CreateCommunityThreadCommand(
            members.owner(), "[QA] " + title, "개발 환경 채팅 QA용 데이터입니다.", category, "💬",
            List.of(members.web(), members.mobile(), members.design())
        )).threadId();
    }

    private Long send(Long threadId, Long senderId, String messageKey, String content, Long replyToMessageId) {
        UUID clientMessageId = UUID.nameUUIDFromBytes(
            ("qa-chat:" + threadId + ":" + messageKey).getBytes(StandardCharsets.UTF_8));
        // 첨부·멘션 없이 텍스트만 생성하되 마지막 메시지와 미읽음 상태는 실제 서비스 로직으로 갱신한다.
        return createMessageUseCase.create(new CreateCommunityThreadMessageCommand(
            threadId, senderId, clientMessageId, CommunityThreadMessageType.TEXT, content,
            List.of(), List.of(), replyToMessageId
        )).message().messageId();
    }

    private record Participants(Long owner, Long web, Long mobile, Long design) {
    }
}
