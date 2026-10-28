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
    @Operation(operationId = "PROJECT-401", summary = "수동 배정 후보 조회",
        description = "해당 기수·매칭 지부의 파트별 미배정 챌린저를 조회합니다. 발표 전 선발 인원과 합격 지원서 보유자는 제외합니다. "
            + "랜덤 매칭 조정 기간에는 조정 명단 기준으로 판단합니다.")
    public PageResponse<ProjectMatchingCandidateInfo> candidates(@CurrentMember MemberPrincipal principal,
        @PathVariable Long projectId, @RequestParam ChallengerPart part,
        @RequestParam(required = false) String keyword,
        @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(searchProjectMatchingCandidateUseCase.search(
            projectId, principal.getMemberId(), part, keyword, pageable), info -> info);
    }

    @PostMapping
    @Operation(operationId = "PROJECT-402", summary = "운영진 수동 배정",
        description = """
            현재 파트와 매칭 지부를 검증하며 TO 초과·중복 배정은 거부합니다.
            TO는 배정된 팀원과 PM이 합격 처리했지만 아직 팀원으로 반영되지 않은 지원자를 포함해 검증합니다.
            배정된 팀원으로 TO가 가득 찼으면 409(PROJECT-0106), 미반영 합격자를 포함해 가득 찼으면 409(PROJECT-0107)를 반환합니다.

            배정 가능 기간
            디자이너(DESIGN)는 PLAN_DESIGN 매칭 차수, 개발자(WEB 등 개발 파트)는 PLAN_DEVELOPER 매칭 차수일때만 수동 배정이 가능합니다.
            - 1차 지원 시작부터 1차 PM 결정 마감 전까지
            - 1차 결과 공개부터 2차 PM 결정 마감 전까지
            - 2차 결과 공개부터 3차 PM 결정 마감 전까지
            - 랜덤 매칭 완료 후 조정 시작부터 조정 종료 전까지 (최종 결과 공개 시 공개 명단에 반영)

            그 외 기간에는 409(PROJECT-0103)를 반환합니다.
            """)
    public Long assign(@CurrentMember MemberPrincipal principal, @PathVariable Long projectId,
        @Valid @RequestBody AddProjectMemberRequest request) {
        return manageProjectMatchingUseCase.assign(request.toCommand(projectId, principal.getMemberId()));
    }

    @DeleteMapping("/{memberId}")
    @Operation(operationId = "PROJECT-403", summary = "운영진 매칭 해제",
        description = """
            팀 소속을 해제하고 연결 지원서는 불합격으로 변경합니다. 지원서·답변은 보존합니다.
            해제 가능 기간은 수동 배정과 같으며, 그 외 기간에는 409(PROJECT-0103)를 반환합니다.
            랜덤 매칭 조정 기간에 공개된 팀원의 해제는 최종 결과 공개 시 반영하고, 조정 기간에 배정한 팀원은 바로 취소합니다.
            """)
    public void unassign(@CurrentMember MemberPrincipal principal, @PathVariable Long projectId,
        @PathVariable Long memberId, @RequestParam(required = false) String reason) {
        manageProjectMatchingUseCase.unassign(RemoveProjectMemberCommand.builder()
            .projectId(projectId).memberId(memberId).requesterMemberId(principal.getMemberId()).reason(reason).build());
    }
}
