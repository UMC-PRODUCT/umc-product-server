package com.umc.product.project.adapter.in.web;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.dto.request.AddProjectMemberRequest;
import com.umc.product.project.application.port.in.command.ManageProjectMatchingUseCase;
import com.umc.product.project.application.port.in.command.dto.RemoveProjectMemberCommand;
import com.umc.product.project.application.port.in.query.SearchProjectMatchingCandidateUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingCandidateInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects/admin/{projectId}/members")
@RequiredArgsConstructor
@Tag(name = "Project | 운영진 수동 매칭", description = "총괄단·담당 지부장이 팀원을 수동 배정하거나 매칭 해제합니다.")
public class ProjectManualMatchingController {
    private final ManageProjectMatchingUseCase manageProjectMatchingUseCase;
    private final SearchProjectMatchingCandidateUseCase searchProjectMatchingCandidateUseCase;

    @GetMapping("/candidates")
    @Operation(
        operationId = "PROJECT-401",
        summary = "수동 배정 후보 조회",
        description = """
            해당 기수·매칭 지부의 파트별 미배정 챌린저를 조회합니다.
            - 이미 팀에 배정됐거나(결과 공개 전 선발 포함) 합격 지원서가 있는 챌린저는 제외합니다.
            - 조정 명단(ADJUSTMENT)을 조회할 수 있는 기간에는 해당 명단을 기준으로 판단하므로, 조정 기간에 해제한 인원도 후보에 포함합니다.
            - 배정·해제가 불가능한 기간에도 조회할 수 있습니다.
            """
    )
    public PageResponse<ProjectMatchingCandidateInfo> candidates(@CurrentMember MemberPrincipal principal,
        @PathVariable Long projectId, @RequestParam ChallengerPart part,
        @RequestParam(required = false) String keyword,
        @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(searchProjectMatchingCandidateUseCase.search(
            projectId, principal.getMemberId(), part, keyword, pageable), info -> info);
    }

    @PostMapping
    @Operation(
        operationId = "PROJECT-402",
        summary = "운영진 수동 배정",
        description = """
            챌린저를 프로젝트에 배정합니다. 반환값은 생성 또는 갱신된 프로젝트 팀원 ID입니다.
            - 현재 파트·매칭 지부·중복 배정·파트별 TO를 검증합니다.
            - 해당 파트의 랜덤 매칭 조정 기간에는 조정 명단(ADJUSTMENT)에 먼저 반영하고, 최종 결과 공개 시 공개 명단(PUBLISHED)에 반영합니다.
              그 외 배정 가능 기간에는 즉시 공개 명단에 반영하며, 최종 결과 공개 이후에도 배정할 수 있습니다.
            - 조정 기간에 해제한 팀원을 같은 팀에 다시 배정하면 해제가 취소됩니다.
            - 다른 팀으로 옮기려면 기존 팀에서 해제[PROJECT-403]한 뒤 배정합니다.
            - 버튼 노출은 프로젝트 권한 응답의 member.canAssign과 파트별 잔여 TO를 기준으로 판단합니다.
            - 배정할 수 없는 기간에는 409 PROJECT-0103을 반환합니다.
            - TO 초과는 409 PROJECT-0213, 이미 배정되었거나 합격 지원서가 있으면 409 PROJECT-0206을 반환합니다.
            """
    )
    public Long assign(@CurrentMember MemberPrincipal principal, @PathVariable Long projectId,
        @Valid @RequestBody AddProjectMemberRequest request) {
        return manageProjectMatchingUseCase.assign(request.toCommand(projectId, principal.getMemberId()));
    }

    @DeleteMapping("/{memberId}")
    @Operation(
        operationId = "PROJECT-403",
        summary = "운영진 매칭 해제",
        description = """
            프로젝트 팀원의 배정을 해제합니다. 지원서·답변은 보존합니다.
            - 해당 파트의 랜덤 매칭 조정 기간에 공개된 팀원을 해제하면 조정 명단(ADJUSTMENT)에서 먼저 제외하고,
              공개 명단(PUBLISHED)과 연결 지원서의 불합격 처리는 최종 결과 공개 시 반영합니다.
            - 조정 기간에 새로 배정한 인원은 해제 즉시 조정 명단에서 제외합니다.
            - 그 외 해제 가능 기간에는 즉시 반영하며, 연결 지원서가 있으면 불합격으로 변경합니다.
            - 버튼 노출은 프로젝트 권한 응답의 member.canUnassign을 기준으로 판단합니다.
            - 해제할 수 없는 기간에는 409 PROJECT-0103을 반환합니다.
            """
    )
    public void unassign(@CurrentMember MemberPrincipal principal, @PathVariable Long projectId,
        @PathVariable Long memberId,
        @Parameter(description = "해제 사유(선택). 감사 로그에 기록하며, 연결 지원서가 있으면 불합격 처리 사유로도 기록합니다.")
        @RequestParam(required = false) String reason) {
        manageProjectMatchingUseCase.unassign(RemoveProjectMemberCommand.builder()
            .projectId(projectId).memberId(memberId).requesterMemberId(principal.getMemberId()).reason(reason).build());
    }
}
