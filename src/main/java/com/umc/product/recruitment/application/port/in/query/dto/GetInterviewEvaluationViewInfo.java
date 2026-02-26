package com.umc.product.recruitment.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

public record GetInterviewEvaluationViewInfo(
    Long assignmentId,
    Long applicationId,
    ApplicationInfo application,
    InterviewQuestionSheetInfo questions,
    LiveEvaluationListInfo liveEvaluations,
    MyInterviewEvaluationInfo myEvaluation // 없으면 null
) {
    public record ApplicationInfo(
        Applicant applicant,
        List<AppliedPart> appliedParts
    ) {
    }

    public record Applicant(String nickname, String name) {
    }

    public record AppliedPart(Integer priority, String key, String label) {
    }

    public record InterviewQuestionSheetInfo(
        List<InterviewQuestionInfo> common,
        List<InterviewQuestionInfo> firstChoice,
        List<InterviewQuestionInfo> secondChoice,
        List<LiveQuestionInfo> live
    ) {
    }

    public record InterviewQuestionInfo(Long questionId, Integer orderNo, String text) {
    }

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

    public record LiveEvaluationListInfo(
        Double avgScore,
        List<LiveEvaluationItem> items
    ) {
    }

    public record LiveEvaluationItem(
        Evaluator evaluator,
        Integer score,
        String comments
    ) {
    }

    public record Evaluator(Long memberId, String nickname, String name) {
    }

    public record MyInterviewEvaluationInfo(
        Long evaluationId,
        Integer score,
        String comments,
        Instant submittedAt
    ) {
    }
}
