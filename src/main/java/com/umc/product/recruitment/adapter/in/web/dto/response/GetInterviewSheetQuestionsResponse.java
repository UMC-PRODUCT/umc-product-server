package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetQuestionsInfo;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;

public record GetInterviewSheetQuestionsResponse(
    PartInfo part,
    List<InterviewQuestionResponse> questions
) {
    public static GetInterviewSheetQuestionsResponse from(GetInterviewSheetQuestionsInfo info) {
        return new GetInterviewSheetQuestionsResponse(
            new PartInfo(info.partKey(), info.partKey().getLabel(), info.questionCount()),
            info.questions().stream()
                .map(q -> new InterviewQuestionResponse(q.questionId(), q.orderNo(), q.questionText()))
                .toList()
        );
    }

    public record PartInfo(PartKey key, String label, int questionCount) {
    }

    public record InterviewQuestionResponse(Long questionId, Integer orderNo, String questionText) {
    }
}
