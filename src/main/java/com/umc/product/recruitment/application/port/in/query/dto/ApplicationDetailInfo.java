package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.domain.ApplicationStatus;
import java.util.List;

public record ApplicationDetailInfo(
        Long applicationId,
        ApplicationStatus status,
        ApplicantInfo applicant,
        FormResponseView formResponse
) {
    public record ApplicantInfo(
            Long memberId,
            String name,
            String email,
            ChallengerPart part
    ) {
    }

    public record FormResponseView(
            Long formResponseId,
            List<AnsweredQuestion> answers
    ) {
        public record AnsweredQuestion(
                Long questionId,
                String questionText,
                Object valueJson // Map<String,Object>
        ) {
        }
    }
}

