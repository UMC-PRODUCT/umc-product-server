package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.CreateLiveQuestionResult;

public record CreateLiveQuestionResponse(
    Long liveQuestionId,
    Integer orderNo,
    String text,
    CreatedBy createdBy,
    Boolean canEdit
) {
    public static CreateLiveQuestionResponse from(CreateLiveQuestionResult r) {
        return new CreateLiveQuestionResponse(
            r.liveQuestionId(),
            r.orderNo(),
            r.text(),
            new CreatedBy(r.createdBy().memberId(), r.createdBy().nickname(), r.createdBy().name()),
            r.canEdit()
        );
    }

    public record CreatedBy(Long memberId, String nickname, String name) {
    }
}
