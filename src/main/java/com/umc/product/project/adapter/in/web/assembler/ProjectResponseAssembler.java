package com.umc.product.project.adapter.in.web.assembler;

import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.global.response.PageResponse;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.response.DraftProjectResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectDetailResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectSummaryResponse;
import com.umc.product.project.application.access.ProjectRoleHelper;
import com.umc.product.project.application.port.in.query.GetProjectUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.domain.ProjectApplicationForm;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Project Response 조립기. Controller에서 여러 UseCase를 조합하는 로직을 캡슐화합니다.
 */
@Component
@RequiredArgsConstructor
public class ProjectResponseAssembler {

    private final GetProjectUseCase getProjectUseCase;
    private final SearchProjectUseCase searchProjectUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final LoadProjectApplicationFormPort loadProjectApplicationFormPort;

    /**
     * PROJECT-001 프로젝트 목록 조회.
     * <p>
     * 마스킹은 row 별로 결정한다 — 본인이 PM 인 row 는 실명, 그 외는 마스킹. Central Core 는 전체 실명.
     */
    public PageResponse<ProjectSummaryResponse> searchFor(
        SearchProjectQuery query,
        SubjectAttributes subject
    ) {
        Page<ProjectInfo> page = searchProjectUseCase.search(query, subject);

        Set<Long> ownerIds = page.getContent().stream()
            .map(ProjectInfo::productOwnerMemberId)
            .collect(Collectors.toSet());
        Map<Long, MemberInfo> memberMap = ownerIds.isEmpty()
            ? Map.of()
            : getMemberUseCase.findAllByIds(ownerIds);

        return PageResponse.of(page, info -> {
            MemberBrief owner = toBrief(memberMap.get(info.productOwnerMemberId()));
            ProjectSummaryResponse response = ProjectSummaryResponse.from(info, owner);
            return ProjectRoleHelper.canSeeFullInfo(subject, info.productOwnerMemberId())
                ? response
                : response.toPublic();
        });
    }

    /**
     * PROJECT-002 프로젝트 상세 조회.
     */
    public ProjectDetailResponse detailFor(Long projectId, SubjectAttributes subject) {
        ProjectInfo info = getProjectUseCase.getById(projectId);

        Map<Long, MemberInfo> memberMap = loadMembers(info);

        MemberBrief owner = toBrief(memberMap.get(info.productOwnerMemberId()));
        List<MemberBrief> coOwners = info.coProductOwnerMemberIds().stream()
            .map(id -> toBrief(memberMap.get(id)))
            .toList();

        ProjectDetailResponse response =
            ProjectDetailResponse.from(info, owner, coOwners, resolveApplicationFormId(projectId));
        return ProjectRoleHelper.canSeeFullInfo(subject, info.productOwnerMemberId())
            ? response
            : response.toPublic();
    }

    /**
     * PROJECT-103 PM의 내 Draft 조회. Draft가 없으면 {@code null} 반환.
     */
    public DraftProjectResponse draftFor(Long memberId, Long gisuId) {
        Optional<ProjectInfo> maybe = getProjectUseCase.findDraftByOwnerAndGisu(memberId, gisuId);
        if (maybe.isEmpty()) {
            return null;
        }
        ProjectInfo info = maybe.get();

        Map<Long, MemberInfo> memberMap = loadMembers(info);

        MemberBrief owner = toBrief(memberMap.get(info.productOwnerMemberId()));
        List<MemberBrief> coOwners = info.coProductOwnerMemberIds().stream()
            .map(id -> toBrief(memberMap.get(id)))
            .toList();

        return DraftProjectResponse.from(info, owner, coOwners, resolveApplicationFormId(info.id()));
    }

    private Long resolveApplicationFormId(Long projectId) {
        return loadProjectApplicationFormPort.findByProjectId(projectId)
            .map(ProjectApplicationForm::getId)
            .orElse(null);
    }

    private Map<Long, MemberInfo> loadMembers(ProjectInfo info) {
        Set<Long> ids = new HashSet<>();
        ids.add(info.productOwnerMemberId());
        ids.addAll(info.coProductOwnerMemberIds());
        return ids.isEmpty() ? Map.of() : getMemberUseCase.findAllByIds(ids);
    }

    private MemberBrief toBrief(MemberInfo info) {
        return info == null ? null : MemberBrief.from(info);
    }
}
