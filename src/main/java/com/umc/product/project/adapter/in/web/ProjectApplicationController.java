package com.umc.product.project.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.assembler.ProjectApplicationResponseAssembler;
import com.umc.product.project.adapter.in.web.dto.request.CreateProjectApplicationRequest;
import com.umc.product.project.adapter.in.web.dto.request.UpdateApplicationAnswersRequest;
import com.umc.product.project.adapter.in.web.dto.response.MyProjectApplicationResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicationStatusResponse;
import com.umc.product.project.application.port.in.command.CreateDraftProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectApplicationDraftUseCase;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectApplicationCommand;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
@Tag(name = "Project | 챌린저 지원서", description = "챌린저의 프로젝트 지원서 Draft 생성 / 임시저장 / 제출 / 조회 (APPLY-001~004)")
public class ProjectApplicationController {

    private final CreateDraftProjectApplicationUseCase createDraftProjectApplicationUseCase;
    private final UpdateProjectApplicationDraftUseCase updateProjectApplicationDraftUseCase;
    private final SubmitProjectApplicationUseCase submitProjectApplicationUseCase;
    private final ProjectApplicationResponseAssembler assembler;

    @PostMapping("/{projectId}/applications")
    @Operation(
        summary = "[APPLY-001] 챌린저 지원서 Draft 생성",
        description = "챌린저가 특정 프로젝트의 지원서를 DRAFT 상태로 생성합니다. 이미 DRAFT 지원서가 있으면 기존 application 정보 반환."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT_APPLICATION,
        resourceId = "#projectId",
        permission = PermissionType.WRITE,
        message = "지원서 생성 권한이 없습니다."
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
        message = "지원서 임시저장 권한이 없습니다."
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
        message = "지원서 제출 권한이 없습니다."
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

    @GetMapping("/me/applications")
    @Operation(
        summary = "[APPLY-004] 본인 지원 내역 목록 조회",
        description = """
            요청자의 챌린저 파트 기준으로 매칭 종류를 자동 결정해 본인 지원 내역을 조회한다.

            정렬: 매칭 라운드 시작일 ASC -> 지원서 갱신일 DESC.
            <p>
            `status` 파라미터 :
            <ul>
              <li>미지정 -> PENDING(임시저장) 제외 전체 (SUBMITTED/APPROVED/REJECTED)</li>
              <li>명시 시 해당 상태만 조회</li>
            </ul>
            <p>
            요청자가 해당 기수 챌린저가 아니거나 PLAN 또는 ADMIN 이면 빈 리스트를 반환한다.
            """
    )
    public List<MyProjectApplicationResponse> getMyApplications(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam Long gisuId,
        @RequestParam(required = false) ProjectApplicationStatus status
    ) {
        GetMyProjectApplicationsQuery query = GetMyProjectApplicationsQuery.builder()
            .requesterMemberId(memberPrincipal.getMemberId())
            .gisuId(gisuId)
            .status(status)
            .build();

        return assembler.myApplicationsFor(query);
    }
}
