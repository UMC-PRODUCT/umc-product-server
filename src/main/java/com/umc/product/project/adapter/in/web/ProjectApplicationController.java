package com.umc.product.project.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.dto.request.UpdateApplicationAnswersRequest;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicationStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 챌린저 지원서", description = "챌린저의 프로젝트 지원서 Draft 생성 / 임시저장 / 제출 / 조회 (APPLY-001~004)")
public class ProjectApplicationController {

    @PostMapping("/{projectId}/applications")
    @Operation(
        summary = "[APPLY-001] 챌린저 지원서 Draft 생성",
        description = "챌린저가 특정 프로젝트의 지원서를 DRAFT 상태로 생성합니다. 이미 DRAFT 지원서가 있으면 기존 application 정보 반환."
    )
    public ProjectApplicationStatusResponse createDraft(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId
    ) {
        return null;
    }

    @PutMapping("/{projectId}/applications/me")
    @Operation(
        summary = "[APPLY-002] 챌린저 지원서 임시저장",
        description = "본문이 곧 답변의 새 전체 상태가 된다. 본인의 DRAFT 지원서에서만 호출 가능."
    )
    public ProjectApplicationStatusResponse updateDraft(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId,
        @Valid @RequestBody UpdateApplicationAnswersRequest request
    ) {
        return null;
    }

    @PostMapping("/{projectId}/applications/me/submit")
    @Operation(
        summary = "[APPLY-003] 챌린저 지원서 최종 제출",
        description = "DRAFT -> SUBMITTED 전이. 필수 답변 누락 시 400. 본인의 DRAFT 지원서에서만 호출 가능."
    )
    public ProjectApplicationStatusResponse submit(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId
    ) {
        return null;
    }
}
