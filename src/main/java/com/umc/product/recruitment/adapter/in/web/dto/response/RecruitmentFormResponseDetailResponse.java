package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormResponseDetailInfo;
import com.umc.product.survey.application.port.in.query.dto.AnswerInfo;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import com.umc.product.survey.domain.enums.QuestionType;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record RecruitmentFormResponseDetailResponse(
        Long recruitmentId,
        Long formId,
        Long formResponseId,
        FormResponseStatus status,     // DRAFT / SUBMITTED
        Instant lastSavedAt,
        Instant submittedAt,
        List<SingleAnswerResponse> answers
) {
    public static RecruitmentFormResponseDetailResponse from(
            Long recruitmentId,
            RecruitmentFormResponseDetailInfo info
    ) {
        return new RecruitmentFormResponseDetailResponse(
                recruitmentId,
                info.formId(),
                info.formResponseId(),
                info.status(),
                info.lastSavedAt(),
                info.submittedAt(),
                info.answers().stream().map(SingleAnswerResponse::from).toList()
        );
    }

    public record SingleAnswerResponse(
            Long questionId,
            Map<String, Object> value,
            QuestionType answeredAsType) {
        public static SingleAnswerResponse from(AnswerInfo info) {
            return new SingleAnswerResponse(
                    info.questionId(),
                    info.value(),
                    info.answeredAsType()
            );
        }
    }
}
