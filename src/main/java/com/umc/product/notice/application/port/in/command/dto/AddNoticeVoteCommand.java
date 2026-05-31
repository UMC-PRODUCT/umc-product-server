package com.umc.product.notice.application.port.in.command.dto;

import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record AddNoticeVoteCommand(
    Long createdMemberId,
    String title,
    boolean isAnonymous,
    boolean allowMultipleChoice,
    Instant startsAt,
    Instant endsAtExclusive,
    List<String> options
) {
    public AddNoticeVoteCommand {
        if (options == null || options.size() < 2 || options.size() > 5) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_VOTE_OPTION_COUNT);
        }
        if (options.stream().anyMatch(s -> s == null || s.trim().isEmpty())) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_VOTE_OPTION_CONTENT);
        }
    }
}
