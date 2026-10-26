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
        description = """
            해당 기수·매칭 지부의 파트별 미배정 챌린저를 조회합니다. 발표 전 선발 인원과 합격 지원서 보유자는 제외합니다.
            랜덤 매칭 이후에는 해당 파트의 조정 명단에서 배정 해제한 인원도 후보에 포함합니다.
            수동 배정·해제가 불가능한 기간에도 후보 조회는 가능합니다.
            """)
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
            해당 지부의 디자인·개발 매칭 중 하나라도 다음 기간에 해당하면 두 유형 모두 배정할 수 없습니다.

            - 1~3차 결정 마감부터 해당 결과 공개 완료 전까지
            - 3차 결과 공개 완료 후 운영진 조정 시작 전까지
            - 운영진 조정 종료부터 최종 결과 공개 완료 전까지

            조정 기간에도 랜덤 매칭이 완료되지 않았으면 배정할 수 없습니다.
            대상 파트의 랜덤 매칭 조정 기간에 배정한 인원은 ADJUSTMENT 명단에 바로 반영하고, 최종 결과 공개 시 PUBLISHED 명단에 반영합니다.
            그 외 허용 기간에는 즉시 반영하며, 최종 결과 공개 이후에도 배정할 수 있습니다.
            """)
    public Long assign(@CurrentMember MemberPrincipal principal, @PathVariable Long projectId,
        @Valid @RequestBody AddProjectMemberRequest request) {
        return manageProjectMatchingUseCase.assign(request.toCommand(projectId, principal.getMemberId()));
    }

    @DeleteMapping("/{memberId}")
    @Operation(operationId = "PROJECT-403", summary = "운영진 매칭 해제",
        description = """
            팀 소속을 해제하고 연결 지원서는 불합격으로 변경합니다. 지원서·답변은 보존합니다.
            해당 지부의 디자인·개발 매칭 중 하나라도 다음 기간에 해당하면 두 유형 모두 해제할 수 없습니다.

            - 1~3차 결정 마감부터 해당 결과 공개 완료 전까지
            - 3차 결과 공개 완료 후 운영진 조정 시작 전까지
            - 운영진 조정 종료부터 최종 결과 공개 완료 전까지

            조정 기간에도 랜덤 매칭이 완료되지 않았으면 해제할 수 없습니다.
            대상 파트의 랜덤 매칭 조정 기간에 해제하면 운영진 조정 명단에 즉시 반영하고, 일반 공개 명단에는 최종 결과 발표 시 반영합니다.
            그 외 배정·해제 가능 기간에는 즉시 반영합니다.
            """)
    public void unassign(@CurrentMember MemberPrincipal principal, @PathVariable Long projectId,
        @PathVariable Long memberId, @RequestParam(required = false) String reason) {
        manageProjectMatchingUseCase.unassign(RemoveProjectMemberCommand.builder()
            .projectId(projectId).memberId(memberId).requesterMemberId(principal.getMemberId()).reason(reason).build());
    }
}
