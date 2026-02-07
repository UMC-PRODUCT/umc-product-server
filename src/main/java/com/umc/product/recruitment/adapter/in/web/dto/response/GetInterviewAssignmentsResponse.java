package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsInfo;
import com.umc.product.recruitment.domain.enums.EvaluationProgressStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record GetInterviewAssignmentsResponse(
    Instant serverNow,
    LocalDate selectedDate,
    PartOption selectedPart,
    List<InterviewAssignmentSlotResponse> interviewAssignmentSlots
) {
    public static GetInterviewAssignmentsResponse from(GetInterviewAssignmentsInfo info) {
        return new GetInterviewAssignmentsResponse(
            info.serverNow(),
            info.selectedDate(),
            info.selectedPart(),
            info.interviewAssignmentSlots().stream().map(InterviewAssignmentSlotResponse::from).toList()
        );
    }

    public record InterviewAssignmentSlotResponse(
        Long assignmentId,
        Slot slot,
        Long applicationId,
        Applicant applicant,
        List<AppliedPart> appliedParts,
        Double documentScore,
        EvaluationProgressStatus evaluationProgressStatus
        // 면접 시작 시간 전: `평가 대기` (WAITING) / 면접 시작 후 + 평가 등록 안 함: `평가 중` (IN_PROGRESS) / 면접 시작 후 + 평가 등록 함: `평가 완료` (DONE)
    ) {
        public static InterviewAssignmentSlotResponse from(GetInterviewAssignmentsInfo.InterviewAssignmentSlotInfo i) {
            return new InterviewAssignmentSlotResponse(
                i.assignmentId(),
                new Slot(i.slot().slotId(), i.slot().date(), i.slot().start(), i.slot().end()),
                i.applicationId(),
                new Applicant(i.applicant().nickname(), i.applicant().name()),
                i.appliedParts().stream()
                    .map(p -> new AppliedPart(p.priority(), p.key(), p.label()))
                    .toList(),
                i.documentScore(),
                i.evaluationProgressStatus()
            );
        }
    }

    public record Slot(Long slotId, LocalDate date, LocalTime start, LocalTime end) {
    }

    public record Applicant(String nickname, String name) {
    }

    public record AppliedPart(Integer priority, String key, String label) {
    }
}
