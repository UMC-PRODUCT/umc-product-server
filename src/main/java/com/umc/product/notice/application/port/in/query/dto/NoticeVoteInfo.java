package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.notice.domain.enums.VoteStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 공지사항 상세 조회 시 사용되는 투표 정보 DTO (Notice 도메인)
 */
public record NoticeVoteInfo(
    Long voteId,
    String title,
    boolean isAnonymous,
    boolean allowMultipleChoice,
    VoteStatus status,
    Instant startsAt,
    Instant endsAtExclusive,
    int totalParticipants,
    List<VoteOptionInfo> options,
    List<Long> mySelectedOptionIds) {
    /**
     * 공지사항 투표 선택지 상세 정보
     */
    public record VoteOptionInfo(
        Long optionId,
        String content,
        int voteCount,
        BigDecimal voteRate,
        List<Long> selectedMemberIds) {
    }
}
