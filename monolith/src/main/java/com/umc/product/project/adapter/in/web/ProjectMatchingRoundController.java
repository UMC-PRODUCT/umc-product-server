package com.umc.product.project.adapter.in.web;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.dto.request.CreateProjectMatchingRoundRequest;
import com.umc.product.project.adapter.in.web.dto.request.SaveProjectMatchingRoundsRequest;
import com.umc.product.project.adapter.in.web.dto.request.UpdateProjectMatchingRoundRequest;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMatchingRoundCreateResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMatchingRoundResponse;
import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.CreateProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.DeleteProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.query.GetProjectMatchingRoundUseCase;
import com.umc.product.project.domain.enums.MatchingType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/project/matching-rounds")
@RequiredArgsConstructor
@Tag(name = "Project | 매칭 차수", description = "프로젝트 매칭 차수를 조회하고 관리합니다.")
public class ProjectMatchingRoundController {

    private final GetProjectMatchingRoundUseCase getProjectMatchingRoundUseCase;
    private final CreateProjectMatchingRoundUseCase createProjectMatchingRoundUseCase;
    private final UpdateProjectMatchingRoundUseCase updateProjectMatchingRoundUseCase;
    private final DeleteProjectMatchingRoundUseCase deleteProjectMatchingRoundUseCase;
    private final AutoDecideProjectMatchingRoundUseCase autoDecideProjectMatchingRoundUseCase;

    @GetMapping
    @Operation(
        operationId = "PROJECT-MATCHING-001",
        summary = "매칭 차수 목록 조회",
        description = """
            매칭 차수 목록을 조회합니다.
            - chapterId가 있으면 해당 지부의 매칭 차수만 startsAt 오름차순으로 반환합니다.
            - type을 지정하면 해당 매칭 유형만 조회하며, 생략하면 전체 유형을 조회합니다.
            - time이 있으면 해당 시점에 지원 가능한 1~3차(startsAt <= time <= endsAt)만 반환합니다. 랜덤 매칭은 제외합니다.
            - time 기준 조회는 chapterId와 함께 요청해야 하며, chapterId 없이 time만 요청하면 400을 반환합니다.
            - 서로 다른 유형이 동시에 진행 중이면 유형별 차수가 함께 반환될 수 있습니다.
            - time이 없고 chapterId도 없으면 전체 매칭 차수를 startsAt 오름차순으로 반환합니다.
            """
    )
    public List<ProjectMatchingRoundResponse> list(
        @RequestParam(required = false) Long chapterId,
        @RequestParam(required = false) MatchingType type,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant time
    ) {
        return getProjectMatchingRoundUseCase.list(chapterId, type, time).stream()
            .map(ProjectMatchingRoundResponse::from)
            .toList();
    }

    @PutMapping("/chapters/{chapterId}")
    @Operation(
        operationId = "PROJECT-MATCHING-104",
        summary = "지부·매칭 유형별 매칭 차수 일괄 등록·수정",
        description = """
            해당 지부·매칭 유형의 1~3차와 랜덤 매칭 일정을 한 번에 등록하거나 수정합니다.
            - 최고관리자, 해당 기수 총괄단, 해당 지부장만 저장할 수 있습니다.
            - type은 한 번만 지정하고, rounds에는 1~3차와 랜덤 매칭을 하나씩 모두 포함합니다. 개수가 4개가 아니면 400, phase가 중복되면 409 PROJECT-0309를 반환합니다.
            - 기존 차수가 없으면 네 차수를 생성하고, 모두 있으면 phase로 찾아 ID를 유지한 채 이름·설명·일정을 수정합니다. 일부만 있으면 409 PROJECT-0309를 반환합니다.
            - 1~3차는 startsAt < endsAt < decisionDeadline < resultPublishAt이어야 합니다.
            - 랜덤 매칭은 decisionDeadline을 받지 않으며 endsAt을 결정 마감으로 저장합니다. startsAt~endsAt은 운영진 조정 기간입니다.
            - resultPublishAt은 결정 마감으로부터 최소 5분 이후여야 하며, 위반 시 400 PROJECT-0301을 반환합니다.
              1~3차 자동 선발이 결정 마감 후 기본 1분 뒤 실행되므로, 발표 전 선발 처리 시간을 확보하기 위한 간격입니다.
            - 다음 차수는 이전 차수의 결정 마감과 발표 시각 이후에 시작해야 하며(409 PROJECT-0309), 같은 유형의 차수끼리 기간이 겹치면 409 PROJECT-0302를 반환합니다.
            - 해당 지부·유형의 1차 매칭 시작 이후에는 수정할 수 없으며 400 PROJECT-0314를 반환합니다.
            - 하나라도 검증에 실패하면 전체 변경을 저장하지 않습니다.
            """
    )
    public List<ProjectMatchingRoundResponse> saveAll(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long chapterId,
        @Valid @RequestBody SaveProjectMatchingRoundsRequest request
    ) {
        return updateProjectMatchingRoundUseCase
            .saveAll(request.toCommands(memberPrincipal.getMemberId(), chapterId)).stream()
            .map(ProjectMatchingRoundResponse::from)
            .toList();
    }

    @DeleteMapping("/chapters/{chapterId}")
    @Operation(
        operationId = "PROJECT-MATCHING-105",
        summary = "지부·매칭 유형별 매칭 차수 일괄 삭제",
        description = """
            해당 지부에서 type으로 지정한 매칭 유형의 차수를 모두 삭제합니다.
            - 최고관리자, 해당 기수 총괄단, 해당 지부장만 삭제할 수 있습니다.
            - 해당 지부·유형의 1차 시작 시각부터는 삭제할 수 없으며 400 PROJECT-0314를 반환합니다.
            - 자동 선발이 실행된 차수가 있으면 400 PROJECT-0306을 반환합니다.
            - 지원서가 연결된 차수가 있으면 409 PROJECT-0304를 반환합니다.
            - 삭제할 차수가 없으면 404 PROJECT-0300을 반환합니다.
            - 하나라도 삭제할 수 없으면 전체 차수를 삭제하지 않습니다.
            """
    )
    public void deleteAll(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long chapterId,
        @RequestParam MatchingType type
    ) {
        deleteProjectMatchingRoundUseCase.deleteAll(chapterId, type, memberPrincipal.getMemberId());
    }

    @PostMapping("/{matchingRoundId}/auto-decide")
    @Operation(
        operationId = "PROJECT-MATCHING-201",
        summary = "매칭 차수 자동 선발 수동 실행",
        description = """
            1~3차는 결정 마감(decisionDeadline) 이후 자동 선발하고, 랜덤 매칭은 같은 지부·유형의 3차 결과 공개 후 실행합니다.
            - 최고관리자, 해당 기수 총괄단, 해당 기수·지부의 지부장만 사용할 수 있습니다.
            - 1~3차는 결정 마감 경과 전, 랜덤 매칭은 3차 결과 공개 전에 실행할 수 없으며 400을 반환합니다.
            - 이미 자동 선발이 실행된 매칭 차수에 대해서는 멱등 (no-op).
            - 1~3차는 정책 매트릭스(디자이너 / 개발자)에 따라 SUBMITTED 우선 random 보충 후 부족하면 REJECTED 까지 override 합니다.
            - 랜덤 매칭은 지원서 없이 해당 유형의 챌린저를 잔여 정원에 배정합니다. 같은 기수에 ACTIVE 또는 PENDING_PUBLICATION 멤버가 있으면 제외합니다.
            - 합격자에게는 ProjectMember 가 자동으로 생성됩니다.
            - 발표 시각이 있는 차수는 PENDING_PUBLICATION 멤버만 생성하며, 지원서의 최종 합불은 결과 공개 시 반영합니다.
            - 발표 시각이 없는 기존 차수는 지원서 합불을 즉시 반영하고 ACTIVE 멤버를 생성합니다.
            """
    )
    public void autoDecide(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long matchingRoundId
    ) {
        autoDecideProjectMatchingRoundUseCase.autoDecide(matchingRoundId, memberPrincipal.getMemberId());
    }

    /**
     * @deprecated PUT /api/v1/project/matching-rounds/chapters/{chapterId}를 사용하세요.
     */
    @Deprecated
    @PostMapping
    @Operation(
        deprecated = true,
        operationId = "PROJECT-MATCHING-101",
        summary = "매칭 차수 생성",
        description = """
            Deprecated: PUT /api/v1/project/matching-rounds/chapters/{chapterId}를 사용하세요.
            매칭 차수를 생성합니다.
            - 해당 지부·유형의 1차가 시작되면 최고관리자도 등록할 수 없으며 400 PROJECT-0314를 반환합니다.
            - 최고관리자, 해당 기수 총괄단, 해당 기수·지부의 지부장만 사용할 수 있습니다.
            - 1~3차는 startsAt < endsAt < decisionDeadline < resultPublishAt 순서를 만족해야 합니다.
            - 랜덤 매칭은 type=PLAN_DESIGN 또는 PLAN_DEVELOPER, phase=RANDOM_MATCHING으로 별도 등록합니다.
            - 랜덤 매칭은 startsAt < endsAt = decisionDeadline < resultPublishAt이며 지원서를 받지 않습니다.
            - 모든 차수의 resultPublishAt은 decisionDeadline으로부터 최소 5분 이후여야 하며, 위반 시 400 PROJECT-0301을 반환합니다.
              1~3차 자동 선발이 PM 마감 후 기본 1분 뒤 실행되므로, 발표 전 선발 처리 시간을 확보하기 위한 간격입니다.
            - 랜덤 매칭은 같은 지부·유형의 3차 결과 공개 후 자동 실행하고 resultPublishAt에 공개합니다.
              startsAt~endsAt은 운영진 조정 기간입니다.
            - 같은 지부 내 기존 매칭 차수와 startsAt ~ decisionDeadline 기간이 중복되면 409를 반환합니다.
            - 동일 지부 내 type + phase 조합은 DB Unique Key로도 중복을 제한합니다.
            """
    )
    public ProjectMatchingRoundCreateResponse create(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody CreateProjectMatchingRoundRequest request
    ) {
        Long matchingRoundId = createProjectMatchingRoundUseCase.create(
            request.toCreateCommand(memberPrincipal.getMemberId()));
        return ProjectMatchingRoundCreateResponse.from(matchingRoundId);
    }

    /**
     * @deprecated PUT /api/v1/project/matching-rounds/chapters/{chapterId}를 사용하세요.
     */
    @Deprecated
    @PatchMapping("/{matchingRoundId}")
    @Operation(
        deprecated = true,
        operationId = "PROJECT-MATCHING-102",
        summary = "매칭 차수 수정",
        description = """
            Deprecated: PUT /api/v1/project/matching-rounds/chapters/{chapterId}를 사용하세요.
            매칭 차수 정보를 부분 수정합니다.
            - 해당 지부·유형의 1차가 시작되면 최고관리자도 수정할 수 없으며 400 PROJECT-0314를 반환합니다.
            - 최고관리자, 해당 기수 총괄단, 해당 기수·지부의 지부장만 사용할 수 있습니다.
            - 수정 요청 본문에는 chapterId를 포함하지 않습니다.
            - 매칭 차수가 소속된 chapterId는 수정할 수 없으며, 권한 및 기간 중복 검증도 기존 chapterId 기준으로 수행합니다.
            - 요청 본문에 제공되지 않은 필드는 기존 매칭 차수 값을 유지합니다.
            - 자동 선발 또는 랜덤 매칭 완료 후에는 이름·설명만 수정할 수 있습니다. 유형·차수·일정 변경은 400 PROJECT-0306을 반환합니다.
            - 최종 일정은 1~3차의 경우 startsAt < endsAt < decisionDeadline < resultPublishAt 순서여야 합니다.
            - 랜덤 매칭은 startsAt < endsAt = decisionDeadline < resultPublishAt 순서여야 합니다.
            - 최종 resultPublishAt은 decisionDeadline으로부터 최소 5분 이후여야 하며, 위반 시 400 PROJECT-0301을 반환합니다.
              1~3차 자동 선발이 PM 마감 후 기본 1분 뒤 실행되므로, 발표 전 선발 처리 시간을 확보하기 위한 간격입니다.
            - 발표 시각이 없는 기존 차수는 resultPublishAt을 생략하면 미설정 상태를 유지합니다.
            - 같은 지부 내 다른 매칭 차수와 startsAt ~ decisionDeadline 기간이 중복되면 409를 반환합니다.
            """
    )
    public void update(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long matchingRoundId,
        @Valid @RequestBody UpdateProjectMatchingRoundRequest request
    ) {
        updateProjectMatchingRoundUseCase.update(
            request.toCommand(matchingRoundId, memberPrincipal.getMemberId()));
    }

    /**
     * @deprecated DELETE /api/v1/project/matching-rounds/chapters/{chapterId}?type={type}를 사용하세요.
     */
    @Deprecated
    @DeleteMapping("/{matchingRoundId}")
    @Operation(
        deprecated = true,
        operationId = "PROJECT-MATCHING-103",
        summary = "매칭 차수 삭제",
        description = """
            Deprecated: DELETE /api/v1/project/matching-rounds/chapters/{chapterId}?type={type}를 사용하세요.
            매칭 차수를 삭제합니다.
            - 해당 지부·유형의 1차가 시작되면 최고관리자도 삭제할 수 없으며 400 PROJECT-0314를 반환합니다.
            - 최고관리자, 해당 기수 총괄단, 해당 기수·지부의 지부장만 사용할 수 있습니다.
            - 해당 매칭 차수를 참조하는 지원서가 하나라도 있으면 삭제할 수 없으며 409를 반환합니다.
            - 자동 선발 또는 랜덤 매칭이 실행된 차수는 지원서 유무보다 먼저 검사하며, 삭제 요청에 400 PROJECT-0306을 반환합니다.
            """
    )
    public void delete(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long matchingRoundId
    ) {
        deleteProjectMatchingRoundUseCase.delete(matchingRoundId, memberPrincipal.getMemberId());
    }
}
