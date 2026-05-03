package com.umc.product.project.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.dto.request.ProjectMatchingRoundRequest;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMatchingRoundCreateResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMatchingRoundResponse;
import com.umc.product.project.application.port.in.command.CreateProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.DeleteProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.query.GetProjectMatchingRoundUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/v1/project-matching-rounds")
@RequiredArgsConstructor
@Tag(name = "Project | 매칭 차수", description = "프로젝트 매칭 차수 조회, 생성, 수정, 삭제")
public class ProjectMatchingRoundController {

    private final GetProjectMatchingRoundUseCase getProjectMatchingRoundUseCase;
    private final CreateProjectMatchingRoundUseCase createProjectMatchingRoundUseCase;
    private final UpdateProjectMatchingRoundUseCase updateProjectMatchingRoundUseCase;
    private final DeleteProjectMatchingRoundUseCase deleteProjectMatchingRoundUseCase;

    @GetMapping
    @Operation(
        summary = "[PROJECT-MATCHING-001] 매칭 차수 목록 조회",
        description = """
            매칭 차수 목록을 조회합니다.
            - chapterId가 있으면 해당 지부의 매칭 차수만 startsAt 오름차순으로 반환합니다.
            - time이 있으면 해당 시점에 지원 가능한 매칭 차수(startsAt <= time <= endsAt)만 반환합니다.
            - time 기준 조회는 결과가 최대 1건이 되도록 chapterId와 함께 요청해야 하며, chapterId 없이 time만 요청하면 400을 반환합니다.
            - time이 없고 chapterId도 없으면 전체 매칭 차수를 startsAt 오름차순으로 반환합니다.
            """
    )
    public List<ProjectMatchingRoundResponse> list(
        @RequestParam(required = false) Long chapterId,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant time
    ) {
        return getProjectMatchingRoundUseCase.list(chapterId, time).stream()
            .map(ProjectMatchingRoundResponse::from)
            .toList();
    }

    @PostMapping
    @Operation(
        summary = "[PROJECT-MATCHING-101] 매칭 차수 생성",
        description = """
            매칭 차수를 생성합니다.
            - 중앙운영사무국 총괄단 이상은 모든 지부에 생성할 수 있습니다.
            - 지부장은 본인 지부에만 생성할 수 있습니다.
            - startsAt < endsAt < decisionDeadline 순서를 만족해야 합니다.
            - 같은 지부 내 기존 매칭 차수와 startsAt ~ decisionDeadline 기간이 중복되면 409를 반환합니다.
            - 동일 지부 내 type + phase 조합은 DB Unique Key로도 중복을 제한합니다.
            """
    )
    public ProjectMatchingRoundCreateResponse create(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody ProjectMatchingRoundRequest request
    ) {
        Long matchingRoundId = createProjectMatchingRoundUseCase.create(
            request.toCreateCommand(memberPrincipal.getMemberId()));
        return ProjectMatchingRoundCreateResponse.from(matchingRoundId);
    }

    @PatchMapping("/{matchingRoundId}")
    @Operation(
        summary = "[PROJECT-MATCHING-102] 매칭 차수 수정",
        description = """
            매칭 차수를 수정합니다.
            - 중앙운영사무국 총괄단 이상은 모든 지부의 매칭 차수를 수정할 수 있습니다.
            - 지부장은 본인 지부의 매칭 차수만 수정할 수 있습니다.
            - 매칭 차수가 소속된 chapterId는 수정할 수 없으며, 기간 중복 검증도 기존 chapterId 기준으로 수행합니다.
            - startsAt < endsAt < decisionDeadline 순서를 만족해야 합니다.
            - 같은 지부 내 다른 매칭 차수와 startsAt ~ decisionDeadline 기간이 중복되면 409를 반환합니다.
            """
    )
    public void update(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long matchingRoundId,
        @Valid @RequestBody ProjectMatchingRoundRequest request
    ) {
        updateProjectMatchingRoundUseCase.update(
            request.toUpdateCommand(matchingRoundId, memberPrincipal.getMemberId()));
    }

    @DeleteMapping("/{matchingRoundId}")
    @Operation(
        summary = "[PROJECT-MATCHING-103] 매칭 차수 삭제",
        description = """
            매칭 차수를 삭제합니다.
            - 중앙운영사무국 총괄단 이상은 모든 지부의 매칭 차수를 삭제할 수 있습니다.
            - 지부장은 본인 지부의 매칭 차수만 삭제할 수 있습니다.
            - 해당 매칭 차수를 참조하는 지원서가 하나라도 있으면 삭제할 수 없으며 409를 반환합니다.
            """
    )
    public void delete(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long matchingRoundId
    ) {
        deleteProjectMatchingRoundUseCase.delete(matchingRoundId, memberPrincipal.getMemberId());
    }
}
