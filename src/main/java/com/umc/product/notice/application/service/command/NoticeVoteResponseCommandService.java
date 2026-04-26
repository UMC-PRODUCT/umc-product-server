package com.umc.product.notice.application.service.command;

import com.umc.product.notice.application.port.in.command.ManageNoticeVoteResponseUseCase;
import com.umc.product.notice.application.port.in.command.dto.SubmitNoticeVoteResponseCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeVoteResponseCommand;
import com.umc.product.notice.application.port.out.LoadNoticeVotePort;
import com.umc.product.notice.domain.NoticeVote;
import com.umc.product.notice.domain.enums.VoteStatus;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.survey.application.port.in.command.ManageFormResponseUseCase;
import com.umc.product.survey.application.port.in.command.dto.AnswerCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.SubmitFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateFormResponseCommand;
import com.umc.product.survey.application.port.in.query.GetVoteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeVoteResponseCommandService implements ManageNoticeVoteResponseUseCase {

    private final LoadNoticeVotePort loadNoticeVotePort;
    private final GetVoteUseCase getVoteUseCase;
    private final ManageFormResponseUseCase manageFormResponseUseCase;

    @Override
    public Long submit(SubmitNoticeVoteResponseCommand command) {
        NoticeVote noticeVote = loadOpenNoticeVote(command.noticeId());
        Long questionId = getVoteUseCase.getPrimaryQuestionId(noticeVote.getVoteId());

        return manageFormResponseUseCase.submitImmediately(
            SubmitFormResponseCommand.builder()
                .formId(noticeVote.getVoteId())
                .respondentMemberId(command.respondentMemberId())
                .answers(List.of(buildAnswerCommand(questionId, command.selectedOptionIds())))
                .build()
        );
    }

    @Override
    public void updateOrCancel(UpdateNoticeVoteResponseCommand command) {
        if (command.selectedOptionIds() == null) {
            throw new NoticeDomainException(NoticeErrorCode.SELECTED_OPTION_IDS_REQUIRED);
        }

        NoticeVote noticeVote = loadOpenNoticeVote(command.noticeId());

        if (command.selectedOptionIds().isEmpty()) {
            manageFormResponseUseCase.deleteResponse(
                DeleteFormResponseCommand.builder()
                    .formId(noticeVote.getVoteId())
                    .respondentMemberId(command.respondentMemberId())
                    .build()
            );
            return;
        }

        Long questionId = getVoteUseCase.getPrimaryQuestionId(noticeVote.getVoteId());
        manageFormResponseUseCase.updateResponse(
            UpdateFormResponseCommand.builder()
                .formId(noticeVote.getVoteId())
                .respondentMemberId(command.respondentMemberId())
                .answers(List.of(buildAnswerCommand(questionId, command.selectedOptionIds())))
                .build()
        );
    }

    private NoticeVote loadOpenNoticeVote(Long noticeId) {
        NoticeVote noticeVote = loadNoticeVotePort.findVoteByNoticeId(noticeId)
            .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_VOTE_NOT_FOUND));

        VoteStatus status = noticeVote.getOpenStatus(Instant.now());
        if (status == VoteStatus.NOT_STARTED) {
            throw new NoticeDomainException(NoticeErrorCode.VOTE_NOT_STARTED);
        }
        if (status == VoteStatus.CLOSED) {
            throw new NoticeDomainException(NoticeErrorCode.VOTE_CLOSED);
        }

        return noticeVote;
    }

    private AnswerCommand buildAnswerCommand(Long questionId, List<Long> selectedOptionIds) {
        return AnswerCommand.builder()
            .questionId(questionId)
            .selectedOptionIds(selectedOptionIds)
            .build();
    }
}
