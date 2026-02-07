package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationDetailInfo;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import com.umc.product.survey.domain.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ApplicationDetailResponse(
    Long applicationId,
    ApplicationStatus status,
    ApplicantResponse applicant,
    List<FormPageResponse> formPages
) {
    public static ApplicationDetailResponse from(ApplicationDetailInfo info) {
        return new ApplicationDetailResponse(
            info.applicationId(),
            info.status(),
            ApplicantResponse.from(info.applicant()),
            info.pages() == null ? List.of() : info.pages().stream().map(FormPageResponse::from).toList()
        );
    }

    @Schema(name = "ApplicantResponse", description = "지원자 기본 정보")
    public record ApplicantResponse(
        Long memberId,
        String name,
        String nickname
    ) {
        static ApplicantResponse from(ApplicationDetailInfo.ApplicantInfo a) {
            if (a == null) {
                return null;
            }
            return new ApplicantResponse(a.memberId(), a.name(), a.nickname());
        }
    }

    @Schema(name = "FormPageResponse", description = "폼 페이지")
    public record FormPageResponse(
        Integer pageNo,
        List<QuestionResponse> questions,
        List<PartQuestionGroupResponse> partQuestions
    ) {
        static FormPageResponse from(ApplicationDetailInfo.PageInfo p) {
            return new FormPageResponse(
                p.pageNo(),
                p.questions() == null ? List.of() : p.questions().stream().map(QuestionResponse::from).toList(),
                p.partQuestions() == null ? List.of()
                    : p.partQuestions().stream().map(PartQuestionGroupResponse::from).toList()
            );
        }
    }

    @Schema(name = "PartQuestionGroupResponse", description = "파트별 질문 그룹")
    public record PartQuestionGroupResponse(
        ChallengerPart part,
        List<QuestionResponse> questions
    ) {
        static PartQuestionGroupResponse from(ApplicationDetailInfo.PartQuestionGroupInfo g) {
            return new PartQuestionGroupResponse(
                g.part(),
                g.questions() == null ? List.of() : g.questions().stream().map(QuestionResponse::from).toList()
            );
        }
    }

    @Schema(name = "QuestionResponse", description = "질문 + 답변")
    public record QuestionResponse(
        Long questionId,
        Integer orderNo,
        QuestionType type,
        String questionText,
        boolean required,
        List<OptionResponse> options,
        AnswerResponse answer
    ) {
        static QuestionResponse from(ApplicationDetailInfo.QuestionInfo q) {
            return new QuestionResponse(
                q.questionId(),
                q.orderNo(),
                q.type(),
                q.questionText(),
                q.required(),
                q.options() == null ? List.of() : q.options().stream().map(OptionResponse::from).toList(),
                q.answer() == null ? null : AnswerResponse.from(q.answer())
            );
        }
    }

    @Schema(name = "OptionResponse", description = "질문 선택지")
    public record OptionResponse(
        Long optionId,
        String content,
        boolean isOther
    ) {
        static OptionResponse from(ApplicationDetailInfo.OptionInfo o) {
            return new OptionResponse(o.optionId(), o.content(), o.isOther());
        }
    }

    @Schema(name = "AnswerResponse", description = "지원자 답변")
    public record AnswerResponse(
        QuestionType answeredAsType,
        String displayText,
        Object rawValue
    ) {
        static AnswerResponse from(ApplicationDetailInfo.AnswerInfo a) {
            return new AnswerResponse(a.answeredAsType(), a.displayText(), a.rawValue());
        }
    }
}
