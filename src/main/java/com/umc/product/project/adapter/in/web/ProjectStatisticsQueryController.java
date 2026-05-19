package com.umc.product.project.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.assembler.ProjectResponseAssembler;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 통계 Query", description = "프로젝트 지원·매칭 현황 조회")
public class ProjectStatisticsQueryController {

    private final ProjectResponseAssembler assembler;

    @GetMapping("/{projectId}/statistics")
    @Operation(
        summary = "[PROJECT-STAT-001] 단건 프로젝트 지원/매칭 현황 조회",
        description = """
            단건 프로젝트의 ACTIVE ProjectMember 목록을 기준으로,
            각 멤버가 해당 프로젝트에 작성한 지원서와 연결된 matchingRound(type/phase)를 반환합니다.
            강제 배정 등으로 지원서가 없는 멤버는 applications 빈 목록으로 반환됩니다.
            """
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.READ,
        message = "프로젝트 지원/매칭 현황 조회 권한이 없습니다."
    )
    public ProjectStatisticsResponse getProjectStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "프로젝트 ID", required = true) @PathVariable Long projectId
    ) {
        return assembler.statisticsForProject(projectId);
    }

    @GetMapping("/statistics")
    @Operation(
        summary = "[PROJECT-STAT-002] 지부 전체 프로젝트 지원/매칭 현황 조회",
        description = """
            chapterId에 속한 전체 프로젝트를 대상으로 ACTIVE ProjectMember 목록과
            각 멤버가 해당 프로젝트에 작성한 지원 이력을 프로젝트별로 반환합니다.
            """
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        permission = PermissionType.READ,
        message = "프로젝트 지원/매칭 현황 조회 권한이 없습니다."
    )
    public List<ProjectStatisticsResponse> listChapterProjectStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "지부 ID", required = true) @RequestParam Long chapterId
    ) {
        return assembler.statisticsForChapter(chapterId);
    }
}
