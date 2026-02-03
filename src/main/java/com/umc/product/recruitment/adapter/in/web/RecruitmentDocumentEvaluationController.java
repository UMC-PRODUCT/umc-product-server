package com.umc.product.recruitment.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.recruitment.adapter.in.web.dto.request.CreateMyEvaluationRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpdateDocumentStatusRequest;
import com.umc.product.recruitment.adapter.in.web.dto.request.UpdateMyEvaluationRequest;
import com.umc.product.recruitment.adapter.in.web.dto.response.ApplicationDetailResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.ApplicationEvaluationsResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.ApplicationListResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.MyEvaluationResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.UpdateDocumentStatusResponse;
import com.umc.product.recruitment.application.port.in.command.CreateMyEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateMyEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateMyEvaluationCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateDocumentStatusCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateMyEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.GetApplicationDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetApplicationEvaluationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetApplicationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.query.UpdateDocumentStatusUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationEvaluationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationEvaluationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyEvaluationQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recruitments/{recruitmentId}/applications")
@RequiredArgsConstructor
@Tag(name = SwaggerTag.Constants.DOC_EVALUATION)
public class RecruitmentDocumentEvaluationController {

    private final GetApplicationDetailUseCase getApplicationDetailUseCase;
    private final GetApplicationListUseCase getApplicationListUseCase;
    private final GetApplicationEvaluationListUseCase getApplicationEvaluationListUseCase;
    private final GetMyEvaluationUseCase getMyEvaluationUseCase;
    private final UpdateMyEvaluationUseCase updateMyEvaluationUseCase;
    private final CreateMyEvaluationUseCase createMyEvaluationUseCase;

    private final UpdateDocumentStatusUseCase updateDocumentStatusUseCase;

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
            @RequestParam(required = false) String part,
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

    @GetMapping("/{applicationId}")
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
    public MyEvaluationResponse getMyEvaluation(
            @PathVariable Long recruitmentId,
            @PathVariable Long applicationId,
            @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long evaluatorId = memberPrincipal.getMemberId();

        var result = getMyEvaluationUseCase.get(
                new GetMyEvaluationQuery(recruitmentId, applicationId, evaluatorId)
        );
        return MyEvaluationResponse.from(result);
    }

    @PatchMapping("/{applicationId}/document-evaluations/me")
    @Operation(
            summary = "(운영진) 지원서에 대한 평가 등록하기 (내 평가 Upsert)",
            description = """
                    로그인한 운영진이 특정 지원서에 대한 평가를 임시저장/제출/재제출합니다.
                    * 지원서 평가에 임시저장이 필요할지를 기획단에 문의해놓은 상태인데, 필요하다는 응답이 오면 임시저장/제출/재제출을 하나의 API로 처리하는 방식으로 (존재한다면 업데이트, 존재하지 않는다면 생성) 구현하면 될 것 같습니다.
                    * 임시저장이 필요하지 않다면, 제출은 post, 재제출은 patch로 나누어 구현해도 좋을 것 같습니다. (post 컨트롤러 및 usecase도 구현해놓았습니다.)
                    """
    )
    public MyEvaluationResponse upsertMyEvaluation(
            @PathVariable Long recruitmentId,
            @PathVariable Long applicationId,
            @RequestBody @Valid UpdateMyEvaluationRequest request,
            @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long evaluatorId = memberPrincipal.getMemberId();
        var result = updateMyEvaluationUseCase.update(
                new UpdateMyEvaluationCommand(
                        recruitmentId,
                        applicationId,
                        evaluatorId,
                        request.score(),
                        request.comments()
                )
        );
        return MyEvaluationResponse.from(result);
    }

    @PostMapping("/{applicationId}/document-evaluations/me")
    @Operation(
            summary = "(운영진) 지원서에 대한 평가 등록하기 (내 평가 최초 생성)",
            description = """
                    로그인한 운영진이 특정 지원서에 대한 평가를 최초 생성합니다.
                    * 지원서 평가에 임시저장이 필요할지를 기획단에 문의해놓은 상태인데, 필요하다는 응답이 오면 임시저장/제출/재제출을 하나의 API로 처리하는 방식으로 (존재한다면 업데이트, 존재하지 않는다면 생성) 구현하면 될 것 같습니다. (PATCH를 사용)
                    * 임시저장이 필요하지 않다면, 제출은 post, 재제출은 patch로 나누어 구현해도 좋을 것 같습니다.
                    """
    )
    public MyEvaluationResponse createMyEvaluation(
            @PathVariable Long recruitmentId,
            @PathVariable Long applicationId,
            @RequestBody @Valid CreateMyEvaluationRequest request,
            @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long evaluatorId = memberPrincipal.getMemberId();
        var result = createMyEvaluationUseCase.create(
                new CreateMyEvaluationCommand(
                        recruitmentId,
                        applicationId,
                        evaluatorId,
                        request.score(),
                        request.comments()
                )
        );
        return MyEvaluationResponse.from(result);
    }

    @PatchMapping("/{applicationId}/document-status")
    @Operation(
            summary = "지원서 합격/불합격 처리하기 (서류 결과)",
            description = """
                    특정 지원서(appilcation)의 상태를 변경합니다.
                    서류 평가 기간, 권한을 검증합니다.
                    (APPLIED -> DOC_PASSED / DOC_FAILED)
                    아직 UI 확정 전이라, 요청/응답 필드가 추후 변경될 수 있습니다.
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
}
