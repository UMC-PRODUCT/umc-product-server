package com.umc.product.recruitment.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.recruitment.adapter.in.web.dto.request.CreateLiveQuestionRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpdateLiveQuestionRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpsertMyInterviewEvaluationRequest;
import com.umc.product.recruitment.adapter.in.web.dto.response.CreateLiveQuestionResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.GetInterviewAssignmentsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.GetInterviewEvaluationViewResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.GetInterviewEvaluationsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.GetInterviewOptionsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.GetLiveQuestionsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.GetMyInterviewEvaluationResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.UpdateLiveQuestionResponse;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.command.CreateLiveQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteLiveQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateLiveQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertMyInterviewEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateLiveQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteLiveQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateLiveQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertMyInterviewEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.GetInterviewAssignmentsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewEvaluationSummaryUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewEvaluationViewUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewOptionsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetLiveQuestionsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyInterviewEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationSummaryQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationViewInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationViewQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewOptionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewOptionsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recruitments/{recruitmentId}/interviews")
@RequiredArgsConstructor
@Tag(name = SwaggerTag.Constants.INTERVIEW_EVALUATION)
public class InterviewEvaluationController {

    private final GetInterviewEvaluationViewUseCase getInterviewEvaluationViewUseCase;
    private final GetMyInterviewEvaluationUseCase getMyInterviewEvaluationUseCase;
    private final UpsertMyInterviewEvaluationUseCase upsertMyInterviewEvaluationUseCase;
    private final GetInterviewEvaluationSummaryUseCase getInterviewEvaluationSummaryUseCase;

    private final GetLiveQuestionsUseCase getLiveQuestionsUseCase;
    private final CreateLiveQuestionUseCase createLiveQuestionUseCase;
    private final UpdateLiveQuestionUseCase updateLiveQuestionUseCase;
    private final DeleteLiveQuestionUseCase deleteLiveQuestionUseCase;

    private final GetInterviewAssignmentsUseCase getInterviewAssignmentsUseCase;
    private final GetInterviewOptionsUseCase getInterviewOptionsUseCase;

    @GetMapping("/assignments/{assignmentId}/view")
    @Operation(
        summary = "실시간 면접 평가 상세 화면 초기 진입",
        description = """
            상세 화면 렌더링에 필요한 데이터를 한 번에 내려줍니다.
            - 지원자 정보/파트 뱃지
            - 사전 질문지(공통/1지망/2지망) + 추가질문
            - 실시간 평가 현황
            - 내 평가(없으면 null)
            """
    )
    public GetInterviewEvaluationViewResponse view(
        @PathVariable Long recruitmentId,
        @PathVariable Long assignmentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        GetInterviewEvaluationViewInfo info = getInterviewEvaluationViewUseCase.get(
            new GetInterviewEvaluationViewQuery(recruitmentId, assignmentId, memberPrincipal.getMemberId())
        );
        return GetInterviewEvaluationViewResponse.from(info);
    }

    @GetMapping("/assignments/{assignmentId}/evaluations/me")
    @Operation(
        summary = "(운영진) 내 면접 평가 조회",
        description = "내 평가가 아직 없다면 myEvaluation이 null로 반환됩니다."
    )
    public GetMyInterviewEvaluationResponse getMyEvaluation(
        @PathVariable Long recruitmentId,
        @PathVariable Long assignmentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        GetMyInterviewEvaluationInfo info = getMyInterviewEvaluationUseCase.get(
            new GetMyInterviewEvaluationQuery(recruitmentId, assignmentId, memberPrincipal.getMemberId())
        );
        return GetMyInterviewEvaluationResponse.from(info); // 내부에서 null 처리
    }

    @PatchMapping("/assignments/{assignmentId}/evaluations/me")
    @Operation(
        summary = "(운영진) 내 면접 평가 제출/재제출",
        description = """
            임시저장 없이 제출만 존재합니다.
            별도의 POST API는 없고, 해당 API에서 upsert(없으면 생성/있으면 업데이트)합니다.
            """
    )
    public GetMyInterviewEvaluationResponse upsertMyEvaluation(
        @PathVariable Long recruitmentId,
        @PathVariable Long assignmentId,
        @RequestBody @Valid UpsertMyInterviewEvaluationRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var info = upsertMyInterviewEvaluationUseCase.upsert(
            new UpsertMyInterviewEvaluationCommand(
                recruitmentId,
                assignmentId,
                memberPrincipal.getMemberId(),
                request.score(),
                request.comments()
            )
        );
        return GetMyInterviewEvaluationResponse.from(info);
    }

    @GetMapping("/assignments/{assignmentId}/evaluations/summary")
    @Operation(
        summary = "실시간 평가 현황 조회(평균/리스트)",
        description = "해당 면접 배정(assignment)에 대한 평가자별 점수/메모 프리뷰 및 평균을 반환합니다."
    )
    public GetInterviewEvaluationsResponse getSummary(
        @PathVariable Long recruitmentId,
        @PathVariable Long assignmentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        GetInterviewEvaluationsInfo info = getInterviewEvaluationSummaryUseCase.get(
            new GetInterviewEvaluationSummaryQuery(recruitmentId, assignmentId, memberPrincipal.getMemberId())
        );
        return GetInterviewEvaluationsResponse.from(info);
    }

    @GetMapping("/assignments/{assignmentId}/live-questions")
    @Operation(summary = "추가 질문(즉석 질문) 조회")
    public GetLiveQuestionsResponse getLiveQuestions(
        @PathVariable Long recruitmentId,
        @PathVariable Long assignmentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        GetLiveQuestionsInfo info = getLiveQuestionsUseCase.get(
            new GetLiveQuestionsQuery(recruitmentId, assignmentId, memberPrincipal.getMemberId())
        );
        return GetLiveQuestionsResponse.from(info);
    }

    @PostMapping("/assignments/{assignmentId}/live-questions")
    @Operation(summary = "추가 질문(즉석 질문) 등록")
    public CreateLiveQuestionResponse createLiveQuestion(
        @PathVariable Long recruitmentId,
        @PathVariable Long assignmentId,
        @RequestBody @Valid CreateLiveQuestionRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var info = createLiveQuestionUseCase.create(
            new CreateLiveQuestionCommand(
                recruitmentId,
                assignmentId,
                memberPrincipal.getMemberId(),
                request.text()
            )
        );
        return CreateLiveQuestionResponse.from(info);
    }

    @PatchMapping("/assignments/{assignmentId}/live-questions/{liveQuestionId}")
    @Operation(summary = "추가 질문(즉석 질문) 수정")
    public UpdateLiveQuestionResponse updateLiveQuestion(
        @PathVariable Long recruitmentId,
        @PathVariable Long assignmentId,
        @PathVariable Long liveQuestionId,
        @RequestBody @Valid UpdateLiveQuestionRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var info = updateLiveQuestionUseCase.update(
            new UpdateLiveQuestionCommand(
                recruitmentId,
                assignmentId,
                liveQuestionId,
                memberPrincipal.getMemberId(),
                request.text()
            )
        );
        return UpdateLiveQuestionResponse.from(info);
    }

    @DeleteMapping("/assignments/{assignmentId}/live-questions/{liveQuestionId}")
    @Operation(summary = "추가 질문(즉석 질문) 삭제")
    public void deleteLiveQuestion(
        @PathVariable Long recruitmentId,
        @PathVariable Long assignmentId,
        @PathVariable Long liveQuestionId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        deleteLiveQuestionUseCase.delete(
            new DeleteLiveQuestionCommand(
                recruitmentId,
                assignmentId,
                liveQuestionId,
                memberPrincipal.getMemberId()
            )
        );
    }

    @GetMapping("/assignments")
    @Operation(
        summary = "실시간 면접 평가 대상 리스트 조회",
        description = "특정 날짜 + 파트 필터 기준 카드 리스트를 조회합니다."
    )
    public GetInterviewAssignmentsResponse getInterviewAssignments(
        @PathVariable Long recruitmentId,
        @RequestParam(required = false) LocalDate date,
        @RequestParam(required = false, defaultValue = "ALL") PartOption part,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        GetInterviewAssignmentsInfo info = getInterviewAssignmentsUseCase.get(
            new GetInterviewAssignmentsQuery(recruitmentId, date, part, memberPrincipal.getMemberId())
        );
        return GetInterviewAssignmentsResponse.from(info);
    }

    @GetMapping("/options")
    @Operation(summary = "실시간 면접 평가용 드롭다운 옵션 조회. 날짜와 파트 옵션을 제공합니다.")
    public GetInterviewOptionsResponse getOptions(
        @PathVariable Long recruitmentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        GetInterviewOptionsInfo info = getInterviewOptionsUseCase.get(
            new GetInterviewOptionsQuery(recruitmentId, memberPrincipal.getMemberId())
        );
        return GetInterviewOptionsResponse.from(info);
    }
}
