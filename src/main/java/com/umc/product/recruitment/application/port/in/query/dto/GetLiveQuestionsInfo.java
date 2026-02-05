package com.umc.product.recruitment.application.port.in.query.dto;

import java.util.List;

public record GetLiveQuestionsInfo(
    List<LiveQuestionInfo> items
) {
    public record LiveQuestionInfo(
        Long liveQuestionId,
        Integer orderNo,
        String text,
        CreatedBy createdBy,
        Boolean canEdit
    ) {
    }

    public record CreatedBy(Long memberId, String nickname, String name) {
    }
}
