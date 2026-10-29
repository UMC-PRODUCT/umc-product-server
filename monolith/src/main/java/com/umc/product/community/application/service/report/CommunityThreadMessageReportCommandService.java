package com.umc.product.community.application.service.report;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.chat.application.port.in.query.GetChatMessageRoomUseCase;
import com.umc.product.chat.application.port.in.query.GetChatMessageUseCase;
import com.umc.product.chat.application.port.in.query.dto.ChatMessageInfo;
import com.umc.product.chat.application.port.in.query.dto.GetChatMessageQuery;
import com.umc.product.chat.application.port.in.query.dto.GetChatMessageRoomQuery;
import com.umc.product.community.application.port.in.command.thread.report.ReportCommunityThreadMessageUseCase;
import com.umc.product.community.application.port.in.command.thread.report.dto.ReportCommunityThreadMessageCommand;
import com.umc.product.community.application.port.in.query.thread.report.dto.CommunityThreadMessageReportReceiptInfo;
import com.umc.product.community.application.port.out.thread.LoadCommunityThreadMemberPort;
import com.umc.product.community.application.port.out.thread.LoadCommunityThreadPort;
import com.umc.product.community.domain.CommunityThread;
import com.umc.product.community.domain.CommunityThreadMember;
import com.umc.product.community.domain.exception.CommunityDomainException;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.report.application.port.in.command.SubmitReportUseCase;
import com.umc.product.report.application.port.in.command.dto.SubmitReportCommand;
import com.umc.product.report.application.port.in.command.dto.SubmitReportResult;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityThreadMessageReportCommandService implements ReportCommunityThreadMessageUseCase {

    private final LoadCommunityThreadPort loadThreadPort;
    private final LoadCommunityThreadMemberPort loadThreadMemberPort;
    private final GetChatMessageRoomUseCase getChatMessageRoomUseCase;
    private final GetChatMessageUseCase getChatMessageUseCase;
    private final SubmitReportUseCase submitReportUseCase;

    @Override
    public CommunityThreadMessageReportReceiptInfo report(ReportCommunityThreadMessageCommand command) {
        Long chatRoomId = getChatMessageRoomUseCase.getRoomId(new GetChatMessageRoomQuery(command.messageId()));
        CommunityThread mappedThread = loadThreadPort.findByChatRoomId(chatRoomId)
            .orElseThrow(() -> new CommunityDomainException(CommunityErrorCode.THREAD_NOT_FOUND));
        CommunityThread thread = loadThreadPort.findByIdForUpdate(mappedThread.getId())
            .filter(foundThread -> Objects.equals(foundThread.getChatRoomId(), chatRoomId))
            .filter(foundThread -> !foundThread.isDeleted())
            .orElseThrow(() -> new CommunityDomainException(CommunityErrorCode.THREAD_NOT_FOUND));

        CommunityThreadMember member = loadThreadMemberPort
            .findByThreadIdAndMemberId(thread.getId(), command.requesterMemberId())
            .orElseThrow(() -> new CommunityDomainException(CommunityErrorCode.THREAD_MEMBER_NOT_FOUND));
        if (!member.isActive()) {
            throw new CommunityDomainException(CommunityErrorCode.THREAD_ACCESS_DENIED);
        }

        ChatMessageInfo message = getChatMessageUseCase.getMessage(new GetChatMessageQuery(
            chatRoomId,
            command.requesterMemberId(),
            command.messageId()
        ));
        if (Objects.equals(message.senderMemberId(), command.requesterMemberId())) {
            throw new ReportDomainException(ReportErrorCode.REPORT_SELF_TARGET);
        }

        SubmitReportResult submitted = submitReportUseCase.submit(SubmitReportCommand.threadMessage(
            command.requesterMemberId(),
            thread.getId(),
            command.messageId(),
            command.reason(),
            targetSnapshot(message, thread)
        ));
        return CommunityThreadMessageReportReceiptInfo.from(
            submitted,
            command.messageId(),
            command.reason()
        );
    }

    private SubmitReportCommand.TargetSnapshot targetSnapshot(ChatMessageInfo message, CommunityThread thread) {
        String content = message.content() == null || message.content().isBlank()
            ? "[" + message.contentType().name() + "]"
            : message.content();
        return new SubmitReportCommand.TargetSnapshot(message.senderMemberId(), content, thread.getTitle());
    }
}
