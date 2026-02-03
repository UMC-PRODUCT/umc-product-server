package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsInfo;
import java.util.List;

public record GetLiveQuestionsResponse(
        List<LiveQuestionResponse> liveQuestionResponses
) {
    public static GetLiveQuestionsResponse from(GetLiveQuestionsInfo info) {
        return new GetLiveQuestionsResponse(info.items().stream().map(LiveQuestionResponse::from).toList());
    }

    public record LiveQuestionResponse(
            Long liveQuestionId,
            Integer orderNo,
            String text,
            CreatedBy createdBy,
            Boolean canEdit
    ) {
        public static LiveQuestionResponse from(GetLiveQuestionsInfo.LiveQuestionInfo i) {
            return new LiveQuestionResponse(
                    i.liveQuestionId(),
                    i.orderNo(),
                    i.text(),
                    new CreatedBy(i.createdBy().memberId(), i.createdBy().nickname(), i.createdBy().name()),
                    i.canEdit()
            );
        }
    }

    public record CreatedBy(Long memberId, String nickname, String name) {
    }
}