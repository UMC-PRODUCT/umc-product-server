package com.umc.product.recruitment.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.recruitment.adapter.in.web.dto.request.CreateRecruitmentRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.RecruitmentListStatusQuery;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpdateRecruitmentInterviewPreferenceRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpsertRecruitmentFormResponseAnswersRequest;
import com.umc.product.recruitment.adapter.in.web.dto.response.ActiveRecruitmentIdResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.DeleteRecruitmentFormResponseResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentApplicationFormResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentDraftFormResponseResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentFormResponseDetailResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentListResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentNoticeResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.SubmitRecruitmentApplicationResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.UpdateRecruitmentInterviewPreferenceResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.UpsertRecruitmentFormResponseAnswersResponse;
import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentDraftFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.SubmitRecruitmentApplicationUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertRecruitmentFormResponseAnswersUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetDraftFormResponseInfo;
import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetRecruitmentDraftCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentFormResponseCommand;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationCommand;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersInfo;
import com.umc.product.recruitment.application.port.in.query.GetActiveRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentApplicationFormUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentFormResponseDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentNoticeUseCase;
import com.umc.product.recruitment.application.port.in.query.RecruitmentListStatus;
import com.umc.product.recruitment.application.port.in.query.dto.ActiveRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetActiveRecruitmentQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentApplicationFormQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentFormResponseDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentNoticeQuery;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormResponseDetailInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentListInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
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
@Tag(name = SwaggerTag.Constants.RECRUITMENT)
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

    @GetMapping("/active-id")
    @Operation(summary = "현재 모집 중인 모집 ID 조회", description = "memberId 기준으로 현재 모집 중인 recruitmentId를 조회합니다. (사용자의 학교, active 기수 기반). 현재 임시로 memberId를 파라미터로 받으며, 실 동작은 토큰 기반으로 동작 예정.")
    public ActiveRecruitmentIdResponse getActiveRecruitmentId(
            // TODO: @CurrentMember(Long memberId) ArgumentResolver 적용 후 교체 예정
            @RequestParam Long memberId
    ) {
        GetActiveRecruitmentQuery query = new GetActiveRecruitmentQuery(memberId);
        ActiveRecruitmentInfo info = getActiveRecruitmentUseCase.get(query);
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
            summary = "지원 폼 응답 최초 생성(없으면 생성, 있으면 반환)",
            description = "지원서 작성 시작 시 호출합니다. 해당 모집에 대한 DRAFT formResponse가 없으면 생성하고, 이미 있으면 기존 formResponse를 반환합니다."
    )
    public RecruitmentDraftFormResponseResponse createOrGetApplicationDraft(
            Long memberId, // auth 적용 후 제거
            @Parameter(description = "모집 ID") @PathVariable Long recruitmentId
    ) {
        CreateOrGetRecruitmentDraftCommand command = new CreateOrGetRecruitmentDraftCommand(recruitmentId, memberId);
        CreateOrGetDraftFormResponseInfo info = createRecruitmentDraftFormResponseUseCase.createOrGet(command);

        return RecruitmentDraftFormResponseResponse.from(
                recruitmentId,
                info.draftFormResponseInfo(),
                info.created()
        );
    }

    @GetMapping("/{recruitmentId}/applications/{formResponseId}")
    @Operation(
            summary = "지원 폼 응답 조회 (작성 중/작성 완료 하나의 API로 처리)",
            description = "formResponseId 기반으로 지원 폼 응답을 단건 조회합니다. 상태(DRAFT/SUBMITTED) 모두 조회 가능합니다."
    )
    public RecruitmentFormResponseDetailResponse getFormResponse(
            Long memberId, // auth 추가 시 제거
            @Parameter(description = "모집 ID") @PathVariable Long recruitmentId,
            @Parameter(description = "폼 응답 ID") @PathVariable Long formResponseId
    ) {
        GetRecruitmentFormResponseDetailQuery query = new GetRecruitmentFormResponseDetailQuery(
                memberId,
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
            @PathVariable Long recruitmentId,
            @PathVariable Long formResponseId,
            @Valid @RequestBody UpsertRecruitmentFormResponseAnswersRequest request
    ) {
        UpsertRecruitmentFormResponseAnswersInfo result = upsertRecruitmentFormResponseAnswersUseCase.upsert(
                request.toCommand(recruitmentId, formResponseId)
        );

        return UpsertRecruitmentFormResponseAnswersResponse.from(result);
    }

    // todo: 추후 "평가하기" 설계하면서 엔티티 등 추가 예정이라, 관련 작업 시에 usecase 넣기
    @PatchMapping("/{recruitmentId}/applications/{formResponseId}/interview-preference")
    @Operation(
            summary = "면접 시간 선호 임시저장",
            description = """
                    지원서 작성 중 면접 가능 시간(스케줄) 선호 정보를 임시저장합니다.
                    """
    )
    public UpdateRecruitmentInterviewPreferenceResponse updateInterviewPreference(
            @Parameter(description = "모집 ID") @PathVariable Long recruitmentId,
            @Parameter(description = "폼 응답 ID") @PathVariable Long formResponseId,
            @Valid @RequestBody UpdateRecruitmentInterviewPreferenceRequest request
    ) {
        Map<String, Object> saved = request.value();

        return UpdateRecruitmentInterviewPreferenceResponse.of(formResponseId, saved);
    }

    @DeleteMapping("/{recruitmentId}/applications/{formResponseId}")
    @Operation(
            summary = "지원 폼 응답 삭제",
            description = """
                    formResponseId에 해당하는 지원 폼 응답을 삭제합니다.
                    "지원하기" UI에서, 기존에 작성하던 응답이 있는 경우 사용자가 "새로작성하기"를 눌렀을 때 호출됩니다.
                    """
    )
    public DeleteRecruitmentFormResponseResponse deleteFormResponse(
            @Parameter(description = "모집 ID") @PathVariable Long recruitmentId,
            @Parameter(description = "폼 응답 ID") @PathVariable Long formResponseId
    ) {
        deleteRecruitmentFormResponseUseCase.delete(
                new DeleteRecruitmentFormResponseCommand(recruitmentId, formResponseId)
        );

        return DeleteRecruitmentFormResponseResponse.of(formResponseId);
    }

    @PostMapping("/{recruitmentId}/applications/{formResponseId}/submit")
    @Operation(
            summary = "지원서 최종 제출",
            description = """
                        지원 폼 응답을 SUBMITTED로 변경하고, recruitment 내부 application을 생성합니다.
                    """
    )
    public SubmitRecruitmentApplicationResponse submitApplication(
            Long memberId, // auth 적용 후 제거
            @Parameter(description = "모집 ID") @PathVariable Long recruitmentId,
            @Parameter(description = "폼 응답 ID") @PathVariable Long formResponseId
    ) {
        SubmitRecruitmentApplicationInfo info = submitRecruitmentApplicationUseCase.submit(
                new SubmitRecruitmentApplicationCommand(recruitmentId, memberId, formResponseId)
        );

        return SubmitRecruitmentApplicationResponse.from(info);
    }

    @PostMapping("")
    @Operation(
            summary = "모집 최초 생성",
            description = """
                    모집 생성 플로우의 첫 화면에서 최초 1회만 호출되는 API입니다. 
                    임시저장 또는 다음 단계 버튼 클릭 시, recruitmentId가 없는 경우에만 이 API를 호출합니다.
                    본 API의 응답으로 반환되는 recruitmentId는 이후 모든 임시저장 및 단계이동 API 호출 시 식별자로 사용됩니다.
                    """
    )
    public Long createRecruitment(
            @RequestBody(required = false) CreateRecruitmentRequest request
    ) {
        CreateRecruitmentRequest req = (request == null) ? CreateRecruitmentRequest.empty() : request;

        CreateRecruitmentCommand command = new CreateRecruitmentCommand(
                req.recruitmentName(),
                req.parts()
        );

        return createRecruitmentUseCase.create(command);
    }

    @GetMapping("")
    @Operation(
            summary = "모집 목록 조회",
            description = """
                    운영진 view에서 모집 목록을 상태에 따라 조회하는 API입니다.
                    """
    )
    public RecruitmentListResponse getRecruitments(
            Long memberId,
            @RequestParam(name = "status") RecruitmentListStatusQuery status
    ) {
        RecruitmentListStatus appStatus = RecruitmentListStatus.valueOf(status.name());

        GetRecruitmentListQuery query = new GetRecruitmentListQuery(memberId, appStatus);

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
            Long memberId,
            @Parameter(description = "모집 ID") @PathVariable Long recruitmentId
    ) {
        DeleteRecruitmentCommand command = new DeleteRecruitmentCommand(memberId, recruitmentId);
        deleteRecruitmentUseCase.delete(command);
    }
}
