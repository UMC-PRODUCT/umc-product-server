package com.umc.product.recruitment.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.recruitment.adapter.in.web.dto.request.CreateExtensionRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.CreateRecruitmentRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.PublishRecruitmentRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.RecruitmentListStatusQuery;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpdatePublishedRecruitmentScheduleRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpdateRecruitmentDraftRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpdateRecruitmentInterviewPreferenceRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpsertRecruitmentFormQuestionsRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpsertRecruitmentFormResponseAnswersRequest;
import com.umc.product.recruitment.adapter.in.web.dto.response.ActiveRecruitmentIdResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.CreateRecruitmentResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.MyRecruitmentApplicationsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.PublishRecruitmentResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentApplicationFormResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentDashboardResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentDraftFormResponseResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentDraftResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentFormResponseDetailResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentListResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentNoticeResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentPartListResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentPublishedResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentSchedulesResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.SubmitRecruitmentApplicationResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.UpdateRecruitmentInterviewPreferenceResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.UpsertRecruitmentFormResponseAnswersResponse;
import com.umc.product.recruitment.application.port.in.command.CreateExtensionCommand;
import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentDraftFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentFormQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentQuestionOptionUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.PublishRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.ResetRecruitmentDraftFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.SubmitRecruitmentApplicationUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdatePublishedRecruitmentScheduleUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateRecruitmentDraftUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateRecruitmentInterviewPreferenceUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertRecruitmentFormQuestionsUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertRecruitmentFormResponseAnswersUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateDraftFormResponseCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateDraftFormResponseInfo;
import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentFormQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentQuestionOptionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentPublishedInfo;
import com.umc.product.recruitment.application.port.in.command.dto.ResetDraftFormResponseCommand;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationCommand;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpdatePublishedRecruitmentScheduleCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentDraftCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentInterviewPreferenceCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentInterviewPreferenceInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersInfo;
import com.umc.product.recruitment.application.port.in.query.GetActiveRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyApplicationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetPublishedRecruitmentDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentApplicationFormUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentDashboardUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentDraftApplicationFormUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentFormResponseDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentNoticeUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentPartListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentScheduleUseCase;
import com.umc.product.recruitment.application.port.in.query.RecruitmentListStatus;
import com.umc.product.recruitment.application.port.in.query.dto.ActiveRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetActiveRecruitmentQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentApplicationFormQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentDraftApplicationFormQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentFormResponseDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentNoticeQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentPartListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentScheduleQuery;
import com.umc.product.recruitment.application.port.in.query.dto.MyApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentDashboardInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormResponseDetailInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentPartListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentScheduleInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/v1/recruitments")
@RequiredArgsConstructor
@Tag(name = "Recruitment | 모집", description = "")
public class RecruitmentController {

    private final GetActiveRecruitmentUseCase getActiveRecruitmentUseCase;
    private final GetRecruitmentNoticeUseCase getRecruitmentNoticeUseCase;
    private final GetRecruitmentApplicationFormUseCase getRecruitmentApplicationFormUseCase;
    private final CreateRecruitmentDraftFormResponseUseCase createRecruitmentDraftFormResponseUseCase;
    private final GetRecruitmentFormResponseDetailUseCase getRecruitmentFormResponseDetailUseCase;
    private final UpsertRecruitmentFormResponseAnswersUseCase upsertRecruitmentFormResponseAnswersUseCase;
    private final DeleteRecruitmentFormResponseUseCase deleteRecruitmentFormResponseUseCase;
    private final SubmitRecruitmentApplicationUseCase submitRecruitmentApplicationUseCase;
    private final CreateRecruitmentUseCase createRecruitmentUseCase;
    private final GetRecruitmentListUseCase getRecruitmentListUseCase;
    private final DeleteRecruitmentUseCase deleteRecruitmentUseCase;
    private final UpdateRecruitmentDraftUseCase updateRecruitmentDraftUseCase;
    private final UpsertRecruitmentFormQuestionsUseCase upsertRecruitmentFormQuestionsUseCase;
    private final PublishRecruitmentUseCase publishRecruitmentUseCase;
    private final GetRecruitmentScheduleUseCase getRecruitmentScheduleUseCase;
    private final GetRecruitmentDashboardUseCase getRecruitmentDashboardUseCase;
    private final GetMyApplicationListUseCase getMyApplicationListUseCase;
    private final GetRecruitmentDetailUseCase getRecruitmentDetailUseCase;
    private final GetRecruitmentPartListUseCase getRecruitmentPartListUseCase;
    private final DeleteRecruitmentFormQuestionUseCase deleteRecruitmentFormQuestionUseCase;
    private final UpdateRecruitmentInterviewPreferenceUseCase updateRecruitmentInterviewPreferenceUseCase;
    private final ResetRecruitmentDraftFormResponseUseCase resetRecruitmentDraftFormResponseUseCase;
    private final GetRecruitmentDraftApplicationFormUseCase getRecruitmentDraftApplicationFormUseCase;
    private final UpdatePublishedRecruitmentScheduleUseCase updatePublishedRecruitmentScheduleUseCase;
    private final GetPublishedRecruitmentDetailUseCase getPublishedRecruitmentDetailUseCase;
    private final DeleteRecruitmentQuestionOptionUseCase deleteRecruitmentQuestionOptionUseCase;

    @GetMapping("/active-id")
    @Operation(summary = "현재 모집 중인 모집 ID 조회", description = "사용자 기준으로 현재 모집 중인 recruitmentId를 조회합니다. (schoolId/gisuId 미지정 시 사용자 학교, active 기수 기반)")
    public ActiveRecruitmentIdResponse getActiveRecruitmentId(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam(required = false) Long schoolId,
        @RequestParam(required = false) Long gisuId
    ) {
        ActiveRecruitmentInfo info = getActiveRecruitmentUseCase.getActiveRecruitment(
            new GetActiveRecruitmentQuery(memberPrincipal.getMemberId(), schoolId, gisuId)
        );
        return ActiveRecruitmentIdResponse.of(info.recruitmentId());
    }

    @GetMapping("/{recruitmentId}/notice")
    @Operation(summary = "모집 공지 조회", description = "모집 안내 화면 상단에 표시할 모집 공지(모집 상세) 정보를 조회합니다.")
    public RecruitmentNoticeResponse getRecruitmentNotice(
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId
    ) {
        GetRecruitmentNoticeQuery query = new GetRecruitmentNoticeQuery(recruitmentId);
        return RecruitmentNoticeResponse.from(getRecruitmentNoticeUseCase.get(query));
    }

    @GetMapping("/{recruitmentId}/application-form")
    @Operation(summary = "지원서 폼 정보 불러오기", description = "지원서 작성 페이지에서 사용할 폼 (질문 목록)을 조회합니다.")
    public RecruitmentApplicationFormResponse getApplicationForm(
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId
    ) {
        GetRecruitmentApplicationFormQuery query = new GetRecruitmentApplicationFormQuery(recruitmentId);
        return RecruitmentApplicationFormResponse.from(getRecruitmentApplicationFormUseCase.get(query));
    }

    @PostMapping("/{recruitmentId}/applications/draft")
    @Operation(
        summary = "지원 폼 응답 최초 생성",
        description = """
            지원서 작성 시작 시 호출합니다.
            - 해당 모집에 대한 DRAFT 지원서가 없으면 새로 생성합니다.
            - 이미 DRAFT가 존재하면 생성하지 않고 오류를 반환합니다. (이어쓰기/새로작성하기는 별도 플로우)
            - 이미 최종 제출(SUBMITTED)한 경우에도 오류를 반환합니다.
            """
    )
    public RecruitmentDraftFormResponseResponse createApplicationDraft(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId
    ) {
        CreateDraftFormResponseCommand command = new CreateDraftFormResponseCommand(
            recruitmentId,
            memberPrincipal.getMemberId()
        );
        CreateDraftFormResponseInfo info = createRecruitmentDraftFormResponseUseCase.create(command);

        return RecruitmentDraftFormResponseResponse.from(
            recruitmentId,
            info.formId(),
            info.formResponseId(),
            info.createdAt()
        );
    }

    @PostMapping("/{recruitmentId}/applications/draft/reset")
    @Operation(
        summary = "지원서 임시본(DRAFT) 초기화 후 재생성",
        description = """
            '새로 작성하기' 동작을 위한 API입니다.
            - 기존 DRAFT 지원서가 있으면 삭제한 뒤 새 DRAFT를 생성합니다.
            - 기존 DRAFT가 없어도 새 DRAFT를 생성합니다.
            - 이미 해당 모집에 지원(최종 제출)한 이력이 있으면 오류를 반환합니다.
            """
    )
    public RecruitmentDraftFormResponseResponse resetDraft(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long recruitmentId
    ) {
        ResetDraftFormResponseCommand command = new ResetDraftFormResponseCommand(
            recruitmentId,
            memberPrincipal.getMemberId()
        );
        CreateDraftFormResponseInfo info = resetRecruitmentDraftFormResponseUseCase.reset(command);

        return RecruitmentDraftFormResponseResponse.from(
            recruitmentId,
            info.formId(),
            info.formResponseId(),
            info.createdAt()
        );
    }

    @GetMapping("/{recruitmentId}/applications/{formResponseId}")
    @Operation(
        summary = "지원 폼 응답 조회 (작성 중/작성 완료 하나의 API로 처리)",
        description = "formResponseId 기반으로 지원 폼 응답을 단건 조회합니다. 상태(DRAFT/SUBMITTED) 모두 조회 가능합니다."
    )
    public RecruitmentFormResponseDetailResponse getFormResponse(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId,
        @Parameter(description = "폼 응답 ID") @PathVariable Long formResponseId
    ) {
        GetRecruitmentFormResponseDetailQuery query = new GetRecruitmentFormResponseDetailQuery(
            memberPrincipal.getMemberId(),
            recruitmentId,
            formResponseId
        );

        RecruitmentFormResponseDetailInfo info = getRecruitmentFormResponseDetailUseCase.get(query);
        return RecruitmentFormResponseDetailResponse.from(recruitmentId, info);
    }

    @PatchMapping("/{recruitmentId}/applications/{formResponseId}/answers")
    @Operation(
        summary = "지원 폼 응답 임시저장 (singleAnswer upsert)",
        description = "formResponseId 기준으로 questionId 단위 답변을 upsert합니다. (단건/다건 모두 가능)"
    )
    public UpsertRecruitmentFormResponseAnswersResponse upsertAnswers(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long recruitmentId,
        @PathVariable Long formResponseId,
        @Valid @RequestBody UpsertRecruitmentFormResponseAnswersRequest request
    ) {
        UpsertRecruitmentFormResponseAnswersInfo result = upsertRecruitmentFormResponseAnswersUseCase.upsert(
            request.toCommand(memberPrincipal.getMemberId(), recruitmentId, formResponseId)
        );

        return UpsertRecruitmentFormResponseAnswersResponse.from(result);
    }

    @Deprecated
    @PatchMapping("/{recruitmentId}/applications/{formResponseId}/interview-preference")
    @Operation(
        summary = "면접 시간 선호 임시저장",
        description = """
            지원서 작성 중 면접 가능 시간(스케줄) 선호 정보를 임시저장합니다.
            """
    )
    public UpdateRecruitmentInterviewPreferenceResponse updateInterviewPreference(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId,
        @Parameter(description = "폼 응답 ID") @PathVariable Long formResponseId,
        @Valid @RequestBody UpdateRecruitmentInterviewPreferenceRequest request
    ) {
        UpdateRecruitmentInterviewPreferenceInfo info = updateRecruitmentInterviewPreferenceUseCase.update(
            new UpdateRecruitmentInterviewPreferenceCommand(
                memberPrincipal.getMemberId(),
                recruitmentId,
                formResponseId,
                request.value()
            )
        );

        return UpdateRecruitmentInterviewPreferenceResponse.of(info.formResponseId(), info.value());
    }

//    @DeleteMapping("/{recruitmentId}/applications/{formResponseId}")
//    @Operation(
//            summary = "지원 폼 응답 삭제",
//            description = """
//                    formResponseId에 해당하는 지원 폼 응답을 삭제합니다.
//                    "지원하기" UI에서, 기존에 작성하던 응답이 있는 경우 사용자가 "새로작성하기"를 눌렀을 때 호출됩니다.
//                    """
//    )
//    public DeleteRecruitmentFormResponseResponse deleteFormResponse(
//            @Parameter(description = "모집 ID") @PathVariable Long recruitmentId,
//            @Parameter(description = "폼 응답 ID") @PathVariable Long formResponseId
//    ) {
//        deleteRecruitmentFormResponseUseCase.delete(
//                new DeleteRecruitmentFormResponseCommand(recruitmentId, formResponseId)
//        );
//
//        return DeleteRecruitmentFormResponseResponse.of(formResponseId);
//    }

    @PostMapping("/{recruitmentId}/applications/{formResponseId}/submit")
    @Operation(
        summary = "지원서 최종 제출",
        description = """
                지원 폼 응답을 SUBMITTED로 변경하고, recruitment 내부 application을 생성합니다.
            """
    )
    public SubmitRecruitmentApplicationResponse submitApplication(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId,
        @Parameter(description = "폼 응답 ID") @PathVariable Long formResponseId
    ) {
        SubmitRecruitmentApplicationInfo info = submitRecruitmentApplicationUseCase.submit(
            new SubmitRecruitmentApplicationCommand(recruitmentId, memberPrincipal.getMemberId(), formResponseId)
        );

        return SubmitRecruitmentApplicationResponse.from(info);
    }

    @PostMapping("")
    @Operation(
        summary = "모집 최초 생성",
        description = """
            모집 생성 플로우의 첫 화면에서 최초 1회만 호출되는 API입니다.
            임시저장 또는 다음 단계 버튼 클릭 시, recruitmentId가 없는 경우에만 이 API를 호출합니다.
            내부적으로 recruitment와 그 recruitment에 매핑되는 form을 생성하고, 응답으로 recruitmentId와 formId를 돌려줍니다.
            본 API의 응답으로 반환되는 recruitmentId는 이후 모든 임시저장 및 단계이동 API 호출 시 식별자로 사용됩니다.
            """
    )
    public CreateRecruitmentResponse createRecruitment(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody(required = false) CreateRecruitmentRequest request
    ) {
        CreateRecruitmentRequest req = (request == null) ? CreateRecruitmentRequest.empty() : request;

        CreateRecruitmentCommand command = new CreateRecruitmentCommand(
            memberPrincipal.getMemberId(),
            req.recruitmentName(),
            req.parts()
        );

        CreateRecruitmentInfo info = createRecruitmentUseCase.create(command);

        return CreateRecruitmentResponse.from(info);
    }

    @PostMapping("/{recruitmentId}/extensions")
    @Operation(
        summary = "추가 모집 최초 생성",
        description = """
            기존 모집(Base)을 기반으로 추가 모집을 생성합니다.
            - 기존 모집의 면접 시간표 및 특정 일정(면접/최종발표 등)을 복제합니다.
            - 기존 모집의 지원서 문항들을 복제하여 새로운 폼을 생성합니다.
            - 추가 모집용 제목과 모집 파트를 새로 설정합니다.
            """
    )
    public CreateRecruitmentResponse createExtension(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "기준이 되는 기존 모집 ID") @PathVariable Long recruitmentId,
        @RequestBody(required = false) CreateExtensionRequest request
    ) {
        CreateExtensionRequest req = (request == null) ? CreateExtensionRequest.empty() : request;

        CreateExtensionCommand command = req.toCommand(recruitmentId, memberPrincipal.getMemberId());
        CreateRecruitmentInfo info = createRecruitmentUseCase.createExtension(command);

        return CreateRecruitmentResponse.from(info);
    }

    @GetMapping("")
    @Operation(
        summary = "모집 목록 조회",
        description = """
            운영진 view에서 모집 목록을 상태에 따라 조회하는 API입니다.
            """
    )
    public RecruitmentListResponse getRecruitments(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam(name = "status") RecruitmentListStatusQuery status
    ) {
        RecruitmentListStatus appStatus = RecruitmentListStatus.fromRequest(status.name());

        GetRecruitmentListQuery query = new GetRecruitmentListQuery(memberPrincipal.getMemberId(), appStatus);

        RecruitmentListInfo info = getRecruitmentListUseCase.getList(query);
        return RecruitmentListResponse.from(info);
    }

    @DeleteMapping("{recruitmentId}")
    @Operation(
        summary = "모집 삭제",
        description = """
            recruitmentId에 해당하는 모집을 삭제합니다.
            """
    )
    public void deleteRecruitment(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId
    ) {
        DeleteRecruitmentCommand command = new DeleteRecruitmentCommand(recruitmentId, memberPrincipal.getMemberId());
        deleteRecruitmentUseCase.delete(command);
    }

    @PatchMapping("/{recruitmentId}")
    @Operation(
        summary = "모집 임시저장(부분 업데이트)",
        description = """
            모집 생성 플로우의 각 단계에서 임시저장 용도로 호출합니다.
            - request body 필드는 모두 optional이며, 전달된 필드만 갱신됩니다.
            - interviewTimeTable은 enabledByDate만 전달합니다.
              disabledByDate는 서버가 dateRange/timeRange/slotMinutes 기준으로 계산하여 응답에 포함합니다.
            - maxPreferredPartCount는 '희망 파트' 질문 설정 변경 시 recruitment에 저장합니다.
            """
    )
    public RecruitmentDraftResponse updateDraftRecruitment(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long recruitmentId,
        @RequestBody(required = false) UpdateRecruitmentDraftRequest request
    ) {
        UpdateRecruitmentDraftRequest req = (request == null) ? UpdateRecruitmentDraftRequest.empty() : request;

        UpdateRecruitmentDraftCommand command = req.toCommand(recruitmentId, memberPrincipal.getMemberId());
        RecruitmentDraftInfo info = updateRecruitmentDraftUseCase.update(command);

        return RecruitmentDraftResponse.from(info);
    }

    @PatchMapping("/{recruitmentId}/application-form")
    @Operation(
        summary = "운영진 지원서 폼 문항 임시저장",
        description = """
            해당 모집의 지원서 폼(FormDefinition)에 질문을 단건/다건 upsert 합니다.
            - questionId 없으면 생성, 있으면 수정
            - target.kind=COMMON_PAGE → pageNo 필수
            - target.kind=PART → part 필수
            """
    )
    public RecruitmentApplicationFormResponse upsertRecruitmentFormQuestions(
        @Parameter(description = "모집 ID", required = true)
        @PathVariable @NotNull Long recruitmentId,
        @Valid @RequestBody UpsertRecruitmentFormQuestionsRequest request
    ) {
        RecruitmentApplicationFormInfo info = upsertRecruitmentFormQuestionsUseCase.upsert(
            request.toCommand(recruitmentId));
        return RecruitmentApplicationFormResponse.from(info);
    }

    @PostMapping("/{recruitmentId}/publish")
    @Operation(
        summary = "모집(지원서 폼) 최종 저장/발행",
        description = """
            모집 draft + 지원서 폼 질문 draft를 최종 반영한 뒤, 발행(PUBLISHED) 처리합니다.
            - recruitmentDraft, applicationFormQuestions 둘 중 하나만 보내도 됩니다(부분 반영 가능).
            - 서버는 최종 반영 후 발행 가능 조건을 검증합니다. 검증 실패 시 발행되지 않습니다.
            """
    )
    public PublishRecruitmentResponse publishRecruitment(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId,
        @RequestBody(required = false) PublishRecruitmentRequest request
    ) {
        PublishRecruitmentRequest req = (request == null) ? PublishRecruitmentRequest.empty() : request;

        var command = req.toCommand(recruitmentId, memberPrincipal.getMemberId());
        var info = publishRecruitmentUseCase.publish(command);

        return PublishRecruitmentResponse.from(info);
    }

    @GetMapping("/{recruitmentId}/schedules")
    @Operation(
        summary = "지원 일정 조회(달력/단계)",
        description = "모집 단계별 기간(서류/면접/평가/결과발표 등)을 달력/단계 UI에서 사용할 수 있도록 조회합니다."
    )
    public RecruitmentSchedulesResponse getRecruitmentSchedules(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId
    ) {
        RecruitmentScheduleInfo info = getRecruitmentScheduleUseCase.get(
            new GetRecruitmentScheduleQuery(recruitmentId, memberPrincipal.getMemberId())
        );
        return RecruitmentSchedulesResponse.from(info);
    }

    @GetMapping("/{recruitmentId}/dashboard")
    @Operation(summary = "대시보드 지원현황 조회(운영진 홈)", description = "일정 요약/진행 단계/지원 현황/평가 현황을 한 번에 조회합니다.")
    public RecruitmentDashboardResponse getDashboard(@PathVariable Long recruitmentId,
                                                     @CurrentMember MemberPrincipal memberPrincipal) {
        RecruitmentDashboardInfo info = getRecruitmentDashboardUseCase.get(recruitmentId,
            memberPrincipal.getMemberId());
        return RecruitmentDashboardResponse.from(info);
    }

    @GetMapping("/me/applications")
    @Operation(summary = "내 지원 현황 조회(지원자 대시보드)")
    public MyRecruitmentApplicationsResponse getMyApplications(
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        GetMyApplicationListQuery query = new GetMyApplicationListQuery(memberPrincipal.getMemberId());
        MyApplicationListInfo info = getMyApplicationListUseCase.get(query);
        return MyRecruitmentApplicationsResponse.from(info);
    }

    @GetMapping("/{recruitmentId}")
    @Operation(
        summary = "모집 불러오기(작성 중 모집 상세 조회, 발행된 모집 조회)",
        description = """
            운영진 모집 생성/편집 화면에서 '작성 이어하기'로 사용할 모집 임시저장본을 조회하거나, 발행된 모집을 조회할 때 이용하는 API입니다.
            모집 정보(제목/파트/일정/공지/타임테이블 등)와 formId를 포함해 반환합니다.
            """
    )
    public RecruitmentDraftResponse getRecruitmentDraftDetail(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId
    ) {
        GetRecruitmentDetailQuery query = new GetRecruitmentDetailQuery(memberPrincipal.getMemberId(), recruitmentId);

        RecruitmentDraftInfo info = getRecruitmentDetailUseCase.get(query);

        return RecruitmentDraftResponse.from(info);
    }

    @GetMapping("/{recruitmentId}/parts")
    @Operation(
        summary = "특정 모집에 대해, 각 파트별 모집 여부 조회 API",
        description = """
            특정 모집(recruitmentId)에 대해 파트별로 모집중/모집마감 상태를 조회합니다.
            지원자 홈에서 파트 리스트(모집중/모집마감 뱃지) 렌더링에 사용합니다.
            """
    )
    public RecruitmentPartListResponse getRecruitmentPartsStatus(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId
    ) {
        GetRecruitmentPartListQuery query = new GetRecruitmentPartListQuery(recruitmentId,
            memberPrincipal.getMemberId());
        RecruitmentPartListInfo info = getRecruitmentPartListUseCase.get(query);
        return RecruitmentPartListResponse.from(info);
    }

    @DeleteMapping("/{recruitmentId}/application-form/questions/{questionId}")
    @Operation(
        summary = "운영진 지원서 폼 단일 문항 삭제",
        description = """
            해당 모집의 지원서 폼(Form)에서 특정 questionId 문항을 삭제합니다.
            - 모집(recruitmentId)에 연결된 formId 기준으로 삭제됩니다.
            """
    )
    public RecruitmentApplicationFormResponse deleteRecruitmentFormQuestion(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long recruitmentId,
        @PathVariable Long questionId
    ) {
        var command = new DeleteRecruitmentFormQuestionCommand(
            recruitmentId,
            memberPrincipal.getMemberId(),
            questionId
        );

        RecruitmentApplicationFormInfo info = deleteRecruitmentFormQuestionUseCase.delete(command);
        return RecruitmentApplicationFormResponse.from(info);
    }

    @GetMapping("/{recruitmentId}/application-form/draft")
    @Operation(
        summary = "운영진 지원서 폼(문항 draft) 조회",
        description = """
            운영진 모집 작성(임시저장) 화면에서, 작성 중인 지원서 폼(문항 draft)만 조회합니다.
            - upsert/delete 응답과 동일한 형태로 반환합니다.
            """
    )
    public RecruitmentApplicationFormResponse getDraftApplicationForm(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "모집 ID") @PathVariable Long recruitmentId
    ) {
        var query = new GetRecruitmentDraftApplicationFormQuery(
            recruitmentId,
            memberPrincipal.getMemberId()
        );
        return RecruitmentApplicationFormResponse.from(
            getRecruitmentDraftApplicationFormUseCase.get(query)
        );
    }

    @PatchMapping("/{recruitmentId}/published")
    @Operation(
        summary = "발행된 모집 수정",
        description = """
            운영진이 배포(PUBLISHED) 상태의 모집 정보를 수정하는 API입니다.

            - 수정 가능 범위
                - 배포(PUBLISHED) 상태의 모집은 일정(schedule) 정보만 수정할 수 있습니다.
              (모집 제목, 파트 구성, 문항, 공지 내용, 면접 가능 타임테이블은 수정할 수 없습니다.)

            - 배포(PUBLISHED) 상태 일정 수정 정책
                - 과거 시점으로의 일정 변경은 허용되지 않습니다.
                - 서류 모집이 시작된 이후에는 서류 모집 시작일을 수정할 수 없습니다.
                - 공고 중인 모집의 마감일을 앞당기는 것은 허용되지 않습니다.
                - 이미 종료된 단계의 일정은 수정할 수 없습니다.
                - 단계 간 일정 순서가 역전되는 변경은 허용되지 않습니다.
                  (예: 면접 시작일이 서류 마감일보다 빠를 수 없음)
                - 서류/최종 결과 발표일은 해당 평가 단계 종료 이후로만 설정할 수 있습니다.
                - 면접 시작/종료 시점은 기존보다 앞당기거나 기간을 단축할 수 없습니다.

            - 면접 시간표(interviewTimeTable) 정책
                - 배포(PUBLISHED) 이후에는 면접 시간표는 수정할 수 없습니다.
                - 이미 제출된 지원자의 면접 가능 시간 및 배치 데이터의 일관성을 보장하기 위함입니다.

            - 기타
                - 본 API는 수정 가능 여부를 서버에서 엄격하게 검증하며, 정책에 위배되는 변경 요청은 오류로 응답합니다.
            """
    )
    public RecruitmentPublishedResponse updatePublishedRecruitmentSchedule(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long recruitmentId,
        @RequestBody UpdatePublishedRecruitmentScheduleRequest request
    ) {
        UpdatePublishedRecruitmentScheduleCommand command =
            request.toCommand(recruitmentId, memberPrincipal.getMemberId());

        RecruitmentPublishedInfo info = updatePublishedRecruitmentScheduleUseCase.update(command);
        return RecruitmentPublishedResponse.from(info);

    }

//    @GetMapping("/{recruitmentId}/published")
//    @Operation(
//            summary = "배포된 모집 상세 조회",
//            description = """
//                    배포(PUBLISHED)된 모집의 상세 정보를 조회합니다.
//
//                    - 운영진의 모집 관리/수정 화면에서 사용합니다.
//                    - 응답에는 모집 기본 정보(제목/파트/공지)와 일정(schedule), 면접 시간표(interviewTimeTable)가 포함됩니다.
//                    - 본 API는 PUBLISHED 상태의 모집만 대상으로 하며, DRAFT는 draft 전용 조회 API를 사용합니다.
//                    """
//    )
//    public RecruitmentPublishedResponse getPublishedRecruitmentDetail(
//            @CurrentMember MemberPrincipal memberPrincipal,
//            @Parameter(description = "모집 ID") @PathVariable Long recruitmentId
//    ) {
//        GetPublishedRecruitmentDetailQuery query =
//                new GetPublishedRecruitmentDetailQuery(memberPrincipal.getMemberId(), recruitmentId);
//
//        RecruitmentPublishedInfo info = getPublishedRecruitmentDetailUseCase.get(query);
//        return RecruitmentPublishedResponse.from(info);
//    }

    @DeleteMapping("/{recruitmentId}/application-form/questions/{questionId}/options/{optionId}")
    @Operation(
        summary = "지원서 폼 질문 옵션 삭제",
        description = """
            운영진이 모집 임시저장(DRAFT) 상태에서 지원서 폼의 특정 질문에 포함된 옵션을 단건 삭제합니다.

            - 모집 상태가 DRAFT인 경우에만 삭제 가능합니다.
            - 해당 모집의 폼에 속한 질문의 옵션만 삭제할 수 있습니다.
            - 삭제 후 최신 지원서 폼 정의 정보를 반환합니다.
            """
    )

    public RecruitmentApplicationFormResponse deleteQuestionOption(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long recruitmentId,
        @PathVariable Long questionId,
        @PathVariable Long optionId
    ) {
        var info = deleteRecruitmentQuestionOptionUseCase.delete(
            new DeleteRecruitmentQuestionOptionCommand(recruitmentId, questionId, optionId,
                memberPrincipal.getMemberId())
        );
        return RecruitmentApplicationFormResponse.from(info);
    }
}
