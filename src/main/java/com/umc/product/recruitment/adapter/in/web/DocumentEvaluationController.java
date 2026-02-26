package com.umc.product.recruitment.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpdateDocumentStatusRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpdateMyDocumentEvaluationRequest;
import com.umc.product.recruitment.adapter.in.web.dto.response.ApplicationDetailResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.ApplicationEvaluationsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.ApplicationListResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.DocumentSelectionApplicationListResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.GetMyDocumentEvaluationResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.UpdateDocumentStatusResponse;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.SortOption;
import com.umc.product.recruitment.application.port.in.command.UpdateMyDocumentEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateDocumentStatusCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateMyDocumentEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.GetApplicationDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetApplicationEvaluationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetApplicationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetDocumentSelectionListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyDocumentEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.query.UpdateDocumentStatusUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationEvaluationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.DocumentSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationEvaluationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetDocumentSelectionApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recruitments/{recruitmentId}/applications")
@RequiredArgsConstructor
@Tag(name = "Recruitment | 서류 평가", description = "")
public class DocumentEvaluationController {

    private final GetApplicationDetailUseCase getApplicationDetailUseCase;
    private final GetApplicationListUseCase getApplicationListUseCase;
    private final GetApplicationEvaluationListUseCase getApplicationEvaluationListUseCase;
    private final GetMyDocumentEvaluationUseCase getMyDocumentEvaluationUseCase;
    private final UpdateMyDocumentEvaluationUseCase updateMyDocumentEvaluationUseCase;
    private final UpdateDocumentStatusUseCase updateDocumentStatusUseCase;
    private final GetDocumentSelectionListUseCase getDocumentSelectionListUseCase;

    @GetMapping("/document-evaluations")
    @Operation(
        summary = "서류 평가 대상 목록 조회",
        description = """
            운영진이 서류 평가 대상 지원서 목록을 조회합니다.
            필터(키워드/파트)와 페이지네이션을 지원합니다.
            """
    )
    public ApplicationListResponse getEvaluationList(
        @PathVariable Long recruitmentId,
        @RequestParam(required = false) PartOption part,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @CurrentMember MemberPrincipal memberPrincipal

    ) {
        ApplicationListInfo info = getApplicationListUseCase.get(
            new GetApplicationListQuery(
                recruitmentId,
                part,
                keyword,
                page,
                size,
                memberPrincipal.getMemberId()
            )
        );
        return ApplicationListResponse.from(info);
    }

    @GetMapping("/{applicationId}/document-evaluation")
    @Operation(
        summary = "(운영진) 지원서 상세 조회",
        description = """
            운영진이 특정 지원서의 상세 정보를 조회합니다.
            운영진 권한 검증이 필요합니다.
            이 api 스웨거 예시 응답이 다른 api 스펙으로 표시되고 있습니다. 정확한 응답 구조는 노션 문서를 확인해주세요.
            """
    )
    public ApplicationDetailResponse getApplicationDetail(
        @PathVariable Long recruitmentId,
        @PathVariable Long applicationId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var result = getApplicationDetailUseCase.get(
            new GetApplicationDetailQuery(recruitmentId, applicationId, memberPrincipal.getMemberId())
        );
        return ApplicationDetailResponse.from(result);
    }


    @GetMapping("/{applicationId}/document-evaluations")
    @Operation(
        summary = "(운영진) 지원서에 대한 운영진 평가 목록 조회",
        description = "특정 지원서에 대해 운영진들이 남긴 서류 평가 내역 목록을 조회합니다."
    )
    public ApplicationEvaluationsResponse getApplicationEvaluations(
        @PathVariable Long recruitmentId,
        @PathVariable Long applicationId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        ApplicationEvaluationListInfo result = getApplicationEvaluationListUseCase.get(
            new GetApplicationEvaluationListQuery(recruitmentId, applicationId, memberPrincipal.getMemberId())
        );
        return ApplicationEvaluationsResponse.from(result);
    }

    @GetMapping("/{applicationId}/document-evaluations/me")
    @Operation(
        summary = "(운영진) 지원서에 대한, 자신의 평가 조회하기",
        description = """
            로그인한 운영진이 특정 지원서에 대해 작성한 '내 평가'를 조회합니다.
            평가가 아직 없으면 null을 반환합니다.
            """
    )
    public GetMyDocumentEvaluationResponse getMyEvaluation(
        @PathVariable Long recruitmentId,
        @PathVariable Long applicationId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long evaluatorId = memberPrincipal.getMemberId();

        var result = getMyDocumentEvaluationUseCase.get(
            new GetMyDocumentEvaluationQuery(recruitmentId, applicationId, evaluatorId)
        );
        return GetMyDocumentEvaluationResponse.from(result);
    }

    @PatchMapping("/{applicationId}/document-evaluations/me")
    @CheckAccess(
        resourceType = ResourceType.RECRUITMENT,
        permission = PermissionType.WRITE
    )
    @Operation(
        summary = "(운영진) 지원서에 대한 평가 등록하기 (내 평가 Upsert)",
        description = """
            로그인한 운영진이 특정 지원서에 대한 평가를 임시저장/제출/재제출합니다.
            임시저장/제출/재제출을 하나의 API로 처리합니다. (존재한다면 업데이트, 존재하지 않는다면 생성)
            """
    )
    public GetMyDocumentEvaluationResponse upsertMyEvaluation(
        @PathVariable Long recruitmentId,
        @PathVariable Long applicationId,
        @RequestBody @Valid UpdateMyDocumentEvaluationRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long evaluatorId = memberPrincipal.getMemberId();
        var result = updateMyDocumentEvaluationUseCase.update(
            new UpdateMyDocumentEvaluationCommand(
                recruitmentId,
                applicationId,
                evaluatorId,
                request.action(),
                request.score(),
                request.comments()
            )
        );
        return GetMyDocumentEvaluationResponse.from(result);
    }

    @PatchMapping("/{applicationId}/document-status")
    @CheckAccess(
        resourceType = ResourceType.RECRUITMENT,
        permission = PermissionType.EDIT
    )
    @Operation(
        summary = "지원서 합격/불합격 처리하기 (서류 결과)",
        description = """
            특정 지원서(application)의 서류 결과를 서류 합격/불합격으로 변경합니다.
            - 프론트 요청/응답용 enum은 PASS / FAIL / WAIT(결정 취소)으로 통일합니다.

            - 상태 변경: APPLIED -> DOC_PASSED / DOC_FAILED
            - 재결정 허용: DOC_PASSED <-> DOC_FAILED
            - 결정 취소(WAIT): DOC_PASSED | DOC_FAILED -> APPLIED
            서류 평가 기간, 권한을 검증합니다.
            """
    )
    public UpdateDocumentStatusResponse updateDocumentStatus(
        @PathVariable Long recruitmentId,
        @PathVariable Long applicationId,
        @RequestBody @Valid UpdateDocumentStatusRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        var result = updateDocumentStatusUseCase.update(
            new UpdateDocumentStatusCommand(
                recruitmentId,
                applicationId,
                request.decision(),
                memberPrincipal.getMemberId()
            )
        );
        return UpdateDocumentStatusResponse.from(result);
    }

    @GetMapping("/document-selections")
    @Operation(
        summary = "서류 선발 대상 리스트 조회",
        description = """
            서류 선발(PASS/FAIL/WAIT) 대상 리스트를 조회합니다.
            part 필터 및 정렬(sort)을 지원합니다.
            """
    )
    public DocumentSelectionApplicationListResponse getDocumentSelections(
        @PathVariable Long recruitmentId,
        @RequestParam(required = false, defaultValue = "ALL") PartOption part,
        @RequestParam(required = false, defaultValue = "SCORE_DESC") SortOption sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        DocumentSelectionApplicationListInfo info = getDocumentSelectionListUseCase.get(
            new GetDocumentSelectionApplicationListQuery(
                recruitmentId,
                part,
                sort,
                page,
                size,
                memberPrincipal.getMemberId()
            )
        );
        return DocumentSelectionApplicationListResponse.from(info);
    }
}
