package com.umc.product.project.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.assembler.ProjectResponseAssembler;
import com.umc.product.project.adapter.in.web.dto.request.SearchProjectRequest;
import com.umc.product.project.adapter.in.web.dto.response.DraftProjectResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectDetailResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMembersResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectSummaryResponse;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 Query", description = "프로젝트 및 관련 정보 조회")
public class ProjectQueryController {

    private final ProjectResponseAssembler assembler;

    @GetMapping
    @Operation(
        summary = "[PROJECT-001] 프로젝트 목록 조회",
        description = "기수/지부/파트 등으로 필터링된 프로젝트 목록을 페이지 조회합니다."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        permission = PermissionType.READ,
        message = "프로젝트 조회 권한이 없습니다."
    )
    public PageResponse<ProjectSummaryResponse> searchProjects(
        @CurrentMember MemberPrincipal memberPrincipal,
        @ParameterObject @Valid SearchProjectRequest request,
        @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        SearchProjectQuery query = request.toQuery(pageable);
        return assembler.searchFor(query, memberPrincipal.getMemberId());
    }

    @GetMapping("/{projectId}")
    @Operation(
        summary = "[PROJECT-002] 프로젝트 상세 조회",
        description = "단건 프로젝트 상세 정보를 조회합니다. 권한에 따라 실명 정보가 마스킹됩니다."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.READ,
        message = "프로젝트 조회 권한이 없습니다."
    )
    public ProjectDetailResponse getDetail(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId
    ) {
        return assembler.detailFor(projectId, memberPrincipal.getMemberId());
    }

    @GetMapping("/{projectId}/members")
    @Operation(
        summary = "[PROJECT-003] 프로젝트 팀원 구성 조회",
        description = "프로젝트의 PM/보조 PM/파트별 멤버를 조회합니다. 권한에 따라 실명이 마스킹됩니다."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.READ,
        message = "프로젝트 조회 권한이 없습니다."
    )
    public ProjectMembersResponse getMembers(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId
    ) {
        return assembler.membersFor(projectId, memberPrincipal.getMemberId());
    }

    @GetMapping("/me/draft")
    @Operation(
        summary = "[PROJECT-103] 내 Draft 조회",
        description = "요청자(PM)가 작성 중인 Draft 프로젝트를 조회합니다. 없으면 null."
    )
    public DraftProjectResponse getMyDraft(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam Long gisuId
    ) {
        return assembler.draftFor(memberPrincipal.getMemberId(), gisuId);
    }
}
