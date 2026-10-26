package com.umc.product.project.adapter.in.web;

import java.util.List;
import java.util.Map;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.project.adapter.in.web.assembler.ProjectResponseAssembler;
import com.umc.product.project.adapter.in.web.dto.request.SearchProjectRequest;
import com.umc.product.project.adapter.in.web.dto.response.DraftProjectResponse;
import com.umc.product.project.adapter.in.web.dto.response.ManagedProjectSummaryResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectDetailResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMembersResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectSummaryResponse;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberView;
import com.umc.product.project.application.port.in.query.dto.SearchManagedProjectQuery;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 Query", description = "프로젝트와 관련 정보를 조회합니다.")
public class ProjectQueryController {

    private final ProjectResponseAssembler assembler;

    @GetMapping
    @Public
    @Operation(
        operationId = "PROJECT-001",
        summary = "프로젝트 목록 조회",
        description = """
            기수/지부/파트 등으로 필터링된 프로젝트 목록을 페이지 조회합니다.
            비회원도 조회 가능하며, 이 경우 공개 상태(IN_PROGRESS/COMPLETED) 프로젝트만 노출됩니다.
            PM 제출시각·ID 오름차순으로 정렬하고, 매칭 기간에는 모집 완료 프로젝트를 하단에 배치합니다.
            디자인·개발 구분은 기존 parts 필터를 사용하며, 생략하면 지부의 진행 중인 매칭 일정을 기준으로 판단합니다.
            """
    )
    public PageResponse<ProjectSummaryResponse> searchProjects(
        @CurrentMember MemberPrincipal memberPrincipal,
        @ParameterObject @Valid SearchProjectRequest request,
        @ParameterObject @PageableDefault(size = 20, sort = {"submittedAt", "id"}, direction = Sort.Direction.ASC)
        Pageable pageable
    ) {
        SearchProjectQuery query = request.toQuery(pageable);
        Long requesterMemberId = memberPrincipal == null ? null : memberPrincipal.getMemberId();
        return assembler.searchFor(query, requesterMemberId);
    }

    @GetMapping("/{projectId}")
    @Public
    @Operation(
        operationId = "PROJECT-002",
        summary = "프로젝트 상세 조회",
        description = "단건 프로젝트 상세 정보를 조회합니다. 비회원도 조회 가능합니다."
    )
    public ProjectDetailResponse getDetail(
        @PathVariable Long projectId
    ) {
        return assembler.detailFor(projectId);
    }

    @GetMapping("/{projectId}/members")
    @Operation(
        operationId = "PROJECT-003",
        summary = "프로젝트 팀원 구성 조회",
        description = "프로젝트의 PM/보조 PM/파트별 멤버를 조회합니다. 권한에 따라 실명이 마스킹됩니다."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.READ,
        message = "프로젝트를 볼 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    public ProjectMembersResponse getMembers(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId
    ) {
        return assembler.membersFor(projectId);
    }

    @GetMapping("/members")
    @Operation(
        operationId = "PROJECT-007",
        summary = "프로젝트 팀원 구성 일괄 조회",
        description = """
            복수의 projectId에 대한 팀원 구성을 조회합니다.
            view를 생략하거나 PUBLISHED로 요청하면 현재 공개된 명단을 반환하며, 권한이 없거나 조회에 실패한 프로젝트는 결과에서 제외됩니다.

            ADJUSTMENT는 랜덤 매칭 이후 운영진의 최종 인원 조정 화면에서 사용합니다.
            랜덤 매칭이 완료되고 조정 시작 시각이 지난 후부터 최종 공개 전까지, 미공개 배정을 포함하고 해제 예약자를 제외한 명단을 반환합니다.
            조정 종료 후에도 최종 공개 전까지 조정 명단을 조회할 수 있으며, 그 외에는 현재 공개된 명단을 반환합니다.
            디자인·개발 매칭 일정은 각각 적용합니다. 요청한 프로젝트 모두에 조정 권한이 필요하며, 하나라도 권한이 없으면 요청 전체를 거부합니다.
            """
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        permission = PermissionType.READ,
        message = "프로젝트를 볼 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    public Map<Long, ProjectMembersResponse> getBatchMembers(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam List<Long> projectIds,
        @Parameter(description = "명단 조회 방식. PUBLISHED: 현재 공개 명단(기본값), ADJUSTMENT: 운영진 최종 인원 조정 화면용 명단")
        @RequestParam(defaultValue = "PUBLISHED") ProjectMemberView view
    ) {
        return assembler.listProjectMembers(projectIds, memberPrincipal.getMemberId(), view);
    }

    @GetMapping("/me/managed")
    @Operation(
        operationId = "PROJECT-006",
        summary = "내가 관리하는 프로젝트 목록",
        description = """
            최고관리자는 모든 기수, 총괄단은 해당 기수 전체 지부, 지부장은 해당 기수 본인 지부를 조회합니다.
            일반 중앙운영·교육국은 해당 기수 전체 지부의 승인대기·등록완료만, PM은 본인 프로젝트를 조회합니다.
            학교 운영진 역할만으로는 관리 목록이 제공되지 않으며, PM 등 다른 정상 역할의 권한은 유지합니다.
            정렬: 등록완료(IN_PROGRESS) → 승인대기(PENDING_REVIEW) → 임시저장(DRAFT) → 기타 상태.
            임시저장은 생성 시각 내림차순이며, 나머지는 요청 정렬(기본: 생성 시각·이름 오름차순)을 적용합니다. 최종 동률은 ID 오름차순입니다.
            """
    )
    public PageResponse<ManagedProjectSummaryResponse> searchManaged(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam Long gisuId,
        @RequestParam(required = false) String keyword,
        @ParameterObject @PageableDefault(size = 20, sort = {"createdAt", "name"}, direction = Sort.Direction.ASC)
        Pageable pageable
    ) {
        SearchManagedProjectQuery query = SearchManagedProjectQuery.builder()
            .gisuId(gisuId)
            .keyword(keyword)
            .pageable(pageable)
            .build();
        return assembler.searchManagedFor(query, memberPrincipal.getMemberId());
    }

    @GetMapping("/me/drafts")
    @Operation(
        operationId = "PROJECT-008",
        summary = "내 초안 프로젝트 목록 조회",
        description = "요청자가 작성한 해당 기수의 초안을 생성 시각·ID 내림차순으로 페이지 조회합니다. 각 초안은 ID로 구분합니다."
    )
    public PageResponse<DraftProjectResponse> getMyDrafts(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam Long gisuId,
        @ParameterObject @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return assembler.draftsFor(memberPrincipal.getMemberId(), gisuId, pageable);
    }

    @GetMapping("/me/draft")
    @Operation(
        operationId = "PROJECT-103",
        summary = "내 초안 프로젝트 조회",
        description = "요청자가 가장 최근 생성한 초안 한 건을 조회합니다. 생성 시각이 같으면 ID가 큰 초안을 반환하며, 없으면 null입니다. 전체 초안은 /me/drafts에서 조회합니다."
    )
    public DraftProjectResponse getMyDraft(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam Long gisuId
    ) {
        return assembler.draftFor(memberPrincipal.getMemberId(), gisuId);
    }
}
