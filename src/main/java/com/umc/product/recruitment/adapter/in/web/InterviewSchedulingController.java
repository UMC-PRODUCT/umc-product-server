package com.umc.product.recruitment.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.recruitment.adapter.in.web.dto.request.CreateInterviewAssignmentRequest;
import com.umc.product.recruitment.adapter.in.web.dto.response.CreateInterviewAssignmentResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.DeleteInterviewAssignmentResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.InterviewSchedulingApplicantsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.InterviewSchedulingAssignmentsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.InterviewSchedulingSlotsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.InterviewSchedulingSummaryResponse;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.command.CreateInterviewAssignmentUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteInterviewAssignmentUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewAssignmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteInterviewAssignmentCommand;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSchedulingApplicantsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSchedulingAssignmentsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSchedulingSlotsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyApplicationListUseCase.GetInterviewSchedulingSummaryUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingApplicantsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingAssignmentsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingSlotsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingSummaryQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recruitments/{recruitmentId}/interviews/scheduling")
@RequiredArgsConstructor
@Tag(name = SwaggerTag.Constants.INTERVIEW_SCHEDULING)
public class InterviewSchedulingController {

    private final GetInterviewSchedulingSummaryUseCase getInterviewSchedulingSummaryUseCase;
    private final GetInterviewSchedulingSlotsUseCase getInterviewSchedulingSlotsUseCase;
    private final GetInterviewSchedulingApplicantsUseCase getInterviewSchedulingApplicantsUseCase;
    private final GetInterviewSchedulingAssignmentsUseCase getInterviewSchedulingAssignmentsUseCase;
    private final CreateInterviewAssignmentUseCase createInterviewAssignmentUseCase;
    private final DeleteInterviewAssignmentUseCase deleteInterviewAssignmentUseCase;

    @GetMapping("/summary")
    @Operation(
        summary = "면접 스케줄링 요약 조회",
        description = """
            면접 스케줄링 화면의 상단 정보(진행률/파트 옵션/룰/컨텍스트)를 조회합니다.
            - date가 null이면 해당 모집의 면접 시작 날짜로 조회합니다.
            - part가 null이면 전체 파트(ALL)로 조회합니다.
                - part 옵션에는 해당 파트의 지원자를 모두 배정 완료 했다는 done을 제공합니다.
            """
    )
    public InterviewSchedulingSummaryResponse getSummary(
        @PathVariable Long recruitmentId,
        @RequestParam(required = false) LocalDate date, // YYYY-MM-DD
        @RequestParam(required = false) PartOption part, // ALL, PLAN, DESIGN, ...
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var info = getInterviewSchedulingSummaryUseCase.get(
            new GetInterviewSchedulingSummaryQuery(recruitmentId, date, part, memberPrincipal.getMemberId())
        );
        return InterviewSchedulingSummaryResponse.from(info);
    }

    @GetMapping("/slots")
    @Operation(
        summary = "면접 슬롯 목록 조회",
        description = """
            좌측 '시간대별 가능 인원' 리스트를 조회합니다.
            - date/part 미입력 시 기본값(면접 시작일/ALL) 기준으로 조회합니다.
            """
    )
    public InterviewSchedulingSlotsResponse getSlots(
        @PathVariable Long recruitmentId,
        @RequestParam(required = false) LocalDate date,
        @RequestParam(required = false) PartOption part,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var info = getInterviewSchedulingSlotsUseCase.get(
            new GetInterviewSchedulingSlotsQuery(recruitmentId, date, part, memberPrincipal.getMemberId())
        );
        return InterviewSchedulingSlotsResponse.from(info);
    }

    @GetMapping("/applicants")
    @Operation(
        summary = "특정 슬롯에 배정 가능한 지원자/이미 배정된 지원자 조회",
        description = """
            선택한 슬롯 기준으로
            - available: 현재 슬롯에 배정 가능한 지원자 목록
            - alreadyScheduled: 다른 슬롯에 이미 배정된 지원자 목록
            를 반환합니다.
            keyword(닉네임/이름 검색) 지원합니다.
            """
    )
    public InterviewSchedulingApplicantsResponse getApplicants(
        @PathVariable Long recruitmentId,
        @RequestParam @NotNull Long slotId,
        @RequestParam(required = false) PartOption part,
        @RequestParam(required = false) String keyword,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var info = getInterviewSchedulingApplicantsUseCase.get(
            new GetInterviewSchedulingApplicantsQuery(recruitmentId, slotId, part, keyword,
                memberPrincipal.getMemberId())
        );
        return InterviewSchedulingApplicantsResponse.from(info);
    }

    @GetMapping("/assignments")
    @Operation(
        summary = "특정 슬롯에 배정된 면접자 목록 조회",
        description = """
            선택한 slotId에 현재 배정된 면접자 목록(items)을 반환합니다.
            part 필터(1지망 기준) 지원합니다.
            """
    )
    public InterviewSchedulingAssignmentsResponse getAssignments(
        @PathVariable Long recruitmentId,
        @RequestParam @NotNull Long slotId,
        @RequestParam(required = false) String part,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var info = getInterviewSchedulingAssignmentsUseCase.get(
            new GetInterviewSchedulingAssignmentsQuery(recruitmentId, slotId, part, memberPrincipal.getMemberId())
        );
        return InterviewSchedulingAssignmentsResponse.from(info);
    }

    @PostMapping("/assignments")
    @Operation(
        summary = "지원자 슬롯 배정",
        description = """
            applicationId를 특정 slotId로 배정합니다.
            배정 결과 + 갱신된 summary를 반환합니다.
            """
    )
    public CreateInterviewAssignmentResponse assign(
        @PathVariable Long recruitmentId,
        @RequestBody @Valid CreateInterviewAssignmentRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var result = createInterviewAssignmentUseCase.create(
            new CreateInterviewAssignmentCommand(
                recruitmentId,
                request.applicationId(),
                request.to().slotId(),
                memberPrincipal.getMemberId()
            )
        );
        return CreateInterviewAssignmentResponse.from(result);
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @Operation(
        summary = "지원자 슬롯 배정 해제",
        description = """
            assignmentId(배정 ID)를 기준으로 배정을 해제합니다.
            해제 결과 + 갱신된 summary를 반환합니다.
            """
    )
    public DeleteInterviewAssignmentResponse unassign(
        @PathVariable Long recruitmentId,
        @PathVariable Long assignmentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var result = deleteInterviewAssignmentUseCase.delete(
            new DeleteInterviewAssignmentCommand(recruitmentId, assignmentId, memberPrincipal.getMemberId())
        );
        return DeleteInterviewAssignmentResponse.from(result);
    }
}
