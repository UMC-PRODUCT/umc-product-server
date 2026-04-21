package com.umc.product.survey.application.port.in.query.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 설문(투표)의 기본 정보 및 참여 현황을 담은 DTO
 */
public record VoteInfo(
    Long formId,
    String title,
    boolean isAnonymous,
    boolean allowMultipleChoice,
    int totalParticipants,
    List<Long> mySelectedOptionIds,
    List<VoteOptionInfo> options
) {
    /**
     * 투표 선택지 상세 정보
     */
    public record VoteOptionInfo(
        Long optionId,
        String content,
        int voteCount,
        BigDecimal voteRate,
        List<Long> selectedMemberIds
    ) {
    }
}
