package com.umc.product.project.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.dto.request.CreateProjectApplicationRequest;
import com.umc.product.project.adapter.in.web.dto.request.UpdateApplicationAnswersRequest;
import com.umc.product.project.adapter.in.web.dto.request.UpdateApplicationDecisionRequest;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicationStatusResponse;
import com.umc.product.project.application.port.in.command.CancelProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.CreateDraftProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.DecideApplicationUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectApplicationDraftUseCase;
import com.umc.product.project.adapter.in.web.dto.request.CreateProjectApplicationRequest;
import com.umc.product.project.application.port.in.command.dto.CancelProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectApplicationCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 챌린저 지원서", description = "챌린저의 프로젝트 지원서 Draft 생성 / 임시저장 / 제출 / 철회 / 조회 (APPLY-001~005)")
public class ProjectApplicationController {

    private final CreateDraftProjectApplicationUseCase createDraftProjectApplicationUseCase;
    private final UpdateProjectApplicationDraftUseCase updateProjectApplicationDraftUseCase;
    private final SubmitProjectApplicationUseCase submitProjectApplicationUseCase;
    private final DecideApplicationUseCase decideApplicationUseCase;
    private final CancelProjectApplicationUseCase cancelProjectApplicationUseCase;

    @PostMapping("/{projectId}/applications")
    @Operation(
        summary = "[APPLY-001] 챌린저 지원서 Draft 생성",
        description = "챌린저가 특정 프로젝트의 지원서를 DRAFT 상태로 생성합니다. 이미 DRAFT 지원서가 있으면 기존 application 정보 반환."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT_APPLICATION,
        resourceId = "#projectId",
        permission = PermissionType.WRITE,
        message = "지원서를 작성할 권한이 없어요. 지원 가능한 프로젝트인지 확인해주세요."
    )
    public ProjectApplicationStatusResponse createDraft(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId,
        @Valid @RequestBody CreateProjectApplicationRequest request
    ) {
        return ProjectApplicationStatusResponse.from(
            createDraftProjectApplicationUseCase.create(
                request.toCommand(projectId, memberPrincipal.getMemberId())
            )
        );
    }

    @PutMapping("/{projectId}/applications/me")
    @Operation(
        summary = "[APPLY-002] 챌린저 지원서 임시저장",
        description = "본문이 곧 답변의 새 전체 상태가 된다. 본인의 DRAFT 지원서에서만 호출 가능."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT_APPLICATION,
        resourceId = "#projectId",
        permission = PermissionType.WRITE,
        message = "지원서를 임시저장할 권한이 없어요. 지원 가능한 프로젝트인지 확인해주세요."
    )
    public ProjectApplicationStatusResponse updateDraft(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId,
        @Valid @RequestBody UpdateApplicationAnswersRequest request
    ) {
        return ProjectApplicationStatusResponse.from(
            updateProjectApplicationDraftUseCase.update(
                request.toCommand(projectId, memberPrincipal.getMemberId())
            )
        );
    }

    @PostMapping("/{projectId}/applications/me/submit")
    @Operation(
        summary = "[APPLY-003] 챌린저 지원서 최종 제출",
        description = "DRAFT -> SUBMITTED 전이. 필수 답변 누락 시 400. 본인의 DRAFT 지원서에서만 호출 가능."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT_APPLICATION,
        resourceId = "#projectId",
        permission = PermissionType.WRITE,
        message = "지원서를 제출할 권한이 없어요. 지원 가능한 프로젝트인지 확인해주세요."
    )
    public ProjectApplicationStatusResponse submit(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId
    ) {
        return ProjectApplicationStatusResponse.from(
            submitProjectApplicationUseCase.submit(
                SubmitProjectApplicationCommand.builder()
                    .projectId(projectId)
                    .requesterMemberId(memberPrincipal.getMemberId())
                    .build()
            )
        );
    }

    @PatchMapping("/{projectId}/applications/{applicationId}/decision")
    @Operation(
        summary = "[APPLY-103] 지원서 합/불 결정 (단일 PATCH)",
        description = """
            PM 이 지원서의 status 를 토글합니다.
            - 매칭 차수 진행 중에만 가능 (decisionDeadline 까지)
            - SUBMITTED ↔ APPROVED ↔ REJECTED 자유 토글 (재토글 허용)
            - PENDING 입력은 도메인의 SUBMITTED 로 매핑되어 결정을 "대기" 로 되돌림
            """
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT_APPLICATION,
        resourceId = "#applicationId",
        permission = PermissionType.APPROVE,
        message = "지원서 합격 여부는 권한이 있는 운영진만 결정할 수 있어요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    public ProjectApplicationStatusResponse decide(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId,
        @PathVariable Long applicationId,
        @Valid @RequestBody UpdateApplicationDecisionRequest request
    ) {
        return ProjectApplicationStatusResponse.from(
            decideApplicationUseCase.decide(
                applicationId, request.status(), request.reason(), memberPrincipal.getMemberId()
            )
        );
    }

    @DeleteMapping("/{projectId}/applications/{applicationId}")
    @Operation(
        summary = "[APPLY-005] 챌린저 지원서 철회",
        description = """
            지원서를 CANCELLED 로 soft delete 합니다.

            정책:
            - 가능 상태: DRAFT, SUBMITTED
            - 불가 상태: APPROVED, REJECTED (이미 종결), CANCELLED (이중 취소)
            - 시간 제약: 지원한 매칭 차수가 OPEN 인 동안만 (startsAt <= now <= endsAt). PM 선발이 시작된 차수 종료 후에는 철회 불가
            - 행위자: 지원자 본인만 (운영진 강제 철회는 별도 API, 추후 작업)

            철회 후 동일 매칭 차수에 재지원 가능 (DB partial unique index 가 활성 지원서 1개 보장).
            Survey 응답 본문은 보존됨.
            """
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT_APPLICATION,
        resourceId = "#applicationId",
        permission = PermissionType.DELETE,
        message = "지원서는 지원자 본인만 철회할 수 있어요."
    )
    public ProjectApplicationStatusResponse cancel(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long applicationId,
        @RequestParam(required = false) String reason
    ) {
        return ProjectApplicationStatusResponse.from(
            cancelProjectApplicationUseCase.cancel(
                CancelProjectApplicationCommand.builder()
                    .applicationId(applicationId)
                    .requesterMemberId(memberPrincipal.getMemberId())
                    .reason(reason)
                    .build()
            )
        );
    }
}
