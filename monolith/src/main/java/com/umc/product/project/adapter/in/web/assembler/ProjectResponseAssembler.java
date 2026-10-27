package com.umc.product.project.adapter.in.web.assembler;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.response.PageResponse;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.response.DraftProjectResponse;
import com.umc.product.project.adapter.in.web.dto.response.ManagedProjectSummaryResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectDetailResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMembersResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMembersResponse.MatchedRoundInfo;
import com.umc.product.project.adapter.in.web.dto.response.ProjectSummaryResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectApplicationStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectMatchingStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse;
import com.umc.product.project.application.port.in.query.GetProjectMatchingAdjustmentUseCase;
import com.umc.product.project.application.port.in.query.GetProjectPublishedMembersUseCase;
import com.umc.product.project.application.port.in.query.GetProjectStatisticsUseCase;
import com.umc.product.project.application.port.in.query.GetProjectUseCase;
import com.umc.product.project.application.port.in.query.SearchDraftProjectUseCase;
import com.umc.product.project.application.port.in.query.SearchManagedProjectUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRosterInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRosterInfo.RosterMemberInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberView;
import com.umc.product.project.application.port.in.query.dto.SearchManagedProjectQuery;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.dto.ProjectMemberMatchedRoundInfo;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMember;

import lombok.RequiredArgsConstructor;

/**
 * Project Response 조립기. Controller에서 여러 UseCase를 조합하는 로직을 캡슐화합니다.
 */
@Component
@RequiredArgsConstructor
public class ProjectResponseAssembler {

    private final GetProjectUseCase getProjectUseCase;
    private final SearchProjectUseCase searchProjectUseCase;
    private final SearchManagedProjectUseCase searchManagedProjectUseCase;
    private final SearchDraftProjectUseCase searchDraftProjectUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final GetProjectStatisticsUseCase getProjectStatisticsUseCase;
    private final GetProjectPublishedMembersUseCase getProjectPublishedMembersUseCase;
    private final GetProjectMatchingAdjustmentUseCase getProjectMatchingAdjustmentUseCase;

    /**
     * PROJECT-001 프로젝트 목록 조회.
     */
    public PageResponse<ProjectSummaryResponse> searchFor(
        SearchProjectQuery query,
        Long requesterMemberId
    ) {
        Page<ProjectInfo> page = searchProjectUseCase.search(query, requesterMemberId);

        Set<Long> ownerIds = page.getContent().stream()
            .map(ProjectInfo::productOwnerMemberId)
            .collect(Collectors.toSet());
        Map<Long, MemberInfo> memberMap = ownerIds.isEmpty()
            ? Map.of()
            : getMemberUseCase.findAllByIds(ownerIds);

        return PageResponse.of(page, info -> {
            MemberBrief owner = toPublicBrief(memberMap.get(info.productOwnerMemberId()));
            return ProjectSummaryResponse.from(info, owner);
        });
    }

    /**
     * PROJECT-002 프로젝트 상세 조회.
     */
    public ProjectDetailResponse detailFor(Long projectId) {
        ProjectInfo info = getProjectUseCase.getById(projectId);

        Map<Long, MemberInfo> memberMap = loadMembers(info);

        MemberBrief owner = toPublicBrief(memberMap.get(info.productOwnerMemberId()));
        List<MemberBrief> coOwners = info.coProductOwnerMemberIds().stream()
            .map(id -> toPublicBrief(memberMap.get(id)))
            .toList();

        return ProjectDetailResponse.from(info, owner, coOwners, resolveApplicationFormId(projectId));
    }

    /**
     * PROJECT-006 관리 화면 프로젝트 목록.
     * <p>
     * 호출자 역할에 따라 자동 scope 적용 (Service 단 {@code resolveForManagement}).
     */
    public PageResponse<ManagedProjectSummaryResponse> searchManagedFor(
        SearchManagedProjectQuery query,
        Long requesterMemberId
    ) {
        Page<ProjectInfo> page = searchManagedProjectUseCase.searchManaged(query, requesterMemberId);

        Set<Long> ownerIds = page.getContent().stream()
            .map(ProjectInfo::productOwnerMemberId)
            .collect(Collectors.toSet());
        Map<Long, MemberInfo> memberMap = ownerIds.isEmpty()
            ? Map.of()
            : getMemberUseCase.findAllByIds(ownerIds);

        return PageResponse.of(page, info -> {
            MemberBrief owner = toBrief(memberMap.get(info.productOwnerMemberId()));
            return ManagedProjectSummaryResponse.from(info, owner);
        });
    }

    /**
     * PROJECT-003 프로젝트 팀원 구성 조회.
     * <p>
     * 메인 PM 별도 노출 + 보조 PM(PLAN 파트의 다른 멤버) + 그 외 파트별 그룹. 각 그룹은 ProjectMember 생성일 오름차순 정렬.
     */
    public ProjectMembersResponse membersFor(Long projectId) {
        ProjectInfo info = getProjectUseCase.getById(projectId);
        List<ProjectMember> members = loadProjectMemberPort.listByProjectId(projectId);

        Set<Long> memberIds = members.stream().map(ProjectMember::getMemberId).collect(Collectors.toSet());
        memberIds.add(info.productOwnerMemberId());
        Map<Long, MemberInfo> memberMap = memberIds.isEmpty()
            ? Map.of()
            : getMemberUseCase.findAllByIds(memberIds);
        Map<ProjectMemberKey, MatchedRoundInfo> matchedRoundMap =
            loadMatchedRoundMap(Set.of(projectId), memberIds);

        return buildMembersResponse(projectId, info, members, memberMap, matchedRoundMap);
    }

    /**
     * PROJECT-007 프로젝트 팀원 구성 일괄 조회.
     * <p>
     * 공개 명단을 일괄 조회한 결과를 응답으로 변환한다. 권한이 없거나 존재하지 않는 프로젝트는 제외한다.
     */
    public Map<Long, ProjectMembersResponse> listProjectMembers(List<Long> projectIds, Long memberId) {
        Map<Long, ProjectMembersResponse> result = new LinkedHashMap<>();
        getProjectPublishedMembersUseCase.listByProjectIds(projectIds, memberId)
            .forEach((projectId, info) -> result.put(projectId, ProjectMembersResponse.from(info)));
        return result;
    }

    /**
     * PROJECT-007 요청한 조회 방식에 따라 공개 명단 또는 운영진 조정 명단을 응답으로 변환한다.
     */
    public Map<Long, ProjectMembersResponse> listProjectMembers(List<Long> projectIds, Long memberId, ProjectMemberView view) {
        if (view == ProjectMemberView.PUBLISHED) {
            return listProjectMembers(projectIds, memberId);
        }
        Map<Long, ProjectMembersResponse> result = new LinkedHashMap<>();
        getProjectMatchingAdjustmentUseCase.batchGetByProjectIds(projectIds, memberId)
            .forEach((projectId, info) -> result.put(projectId, ProjectMembersResponse.from(info)));
        return result;
    }

    public PageResponse<DraftProjectResponse> draftsFor(Long memberId, Long gisuId, Pageable pageable) {
        Page<ProjectInfo> page = searchDraftProjectUseCase.searchDrafts(memberId, gisuId, pageable);
        Set<Long> memberIds = new HashSet<>();
        Set<Long> projectIds = new HashSet<>();
        for (ProjectInfo info : page) {
            projectIds.add(info.id());
            memberIds.add(info.productOwnerMemberId());
            memberIds.addAll(info.coProductOwnerMemberIds());
        }
        Map<Long, MemberInfo> memberMap = memberIds.isEmpty() ? Map.of() : getMemberUseCase.findAllByIds(memberIds);
        Map<Long, ProjectApplicationForm> forms = projectIds.isEmpty()
            ? Map.of() : loadProjectApplicationFormPort.findAllByProjectIds(projectIds);
        return PageResponse.of(page, info -> {
            List<MemberBrief> coOwners = info.coProductOwnerMemberIds().stream()
                .map(id -> toBrief(memberMap.get(id)))
                .toList();
            ProjectApplicationForm form = forms.get(info.id());
            return DraftProjectResponse.from(info, toBrief(memberMap.get(info.productOwnerMemberId())),
                coOwners, form == null ? null : form.getId());
        });
    }

    /**
     * PROJECT-103 내가 가장 최근 생성한 Draft 조회 (creator 기준). Draft 가 없으면 {@code null} 반환.
     */
    public DraftProjectResponse draftFor(Long memberId, Long gisuId) {
        Optional<ProjectInfo> maybe = getProjectUseCase.findDraftByCreatorAndGisu(memberId, gisuId);
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

    /**
     * PROJECT-STAT-001 단건 프로젝트 지원/매칭 현황.
     */
    public ProjectStatisticsResponse statisticsForProject(Long projectId, Long requesterMemberId) {
        return ProjectStatisticsResponse.from(
            getProjectStatisticsUseCase.getByProjectId(projectId, requesterMemberId));
    }

    /**
     * PROJECT-STAT-002 지부 전체 프로젝트 지원 통계.
     */
    public ProjectApplicationStatisticsResponse applicationStatisticsForChapter(
        Long chapterId, Long requesterMemberId
    ) {
        return ProjectApplicationStatisticsResponse.from(
            getProjectStatisticsUseCase.getApplicationStatisticsByChapterId(chapterId, requesterMemberId));
    }

    /**
     * PROJECT-STAT-002 지정 프로젝트 지원 통계.
     */
    public ProjectApplicationStatisticsResponse applicationStatisticsForProjects(
        List<Long> projectIds, Long requesterMemberId
    ) {
        return ProjectApplicationStatisticsResponse.from(
            getProjectStatisticsUseCase.getApplicationStatisticsByProjectIds(projectIds, requesterMemberId));
    }

    /**
     * PROJECT-STAT-003 지부 공개 프로젝트 매칭 요약.
     */
    public ProjectMatchingStatisticsResponse matchingStatisticsForChapter(Long chapterId) {
        return ProjectMatchingStatisticsResponse.from(
            getProjectStatisticsUseCase.getMatchingStatisticsByChapterId(chapterId));
    }

    private ProjectMembersResponse buildMembersResponse(
        Long projectId,
        ProjectInfo info,
        List<ProjectMember> members,
        Map<Long, MemberInfo> memberMap,
        Map<ProjectMemberKey, MatchedRoundInfo> matchedRoundMap
    ) {
        RosterMemberInfo productOwner = toRosterMemberInfo(
            memberMap.get(info.productOwnerMemberId()),
            ChallengerPart.PLAN,
            matchedRoundMap.get(ProjectMemberKey.of(projectId, info.productOwnerMemberId()))
        );
        List<RosterMemberInfo> roster = members.stream()
            .sorted(Comparator.comparing(ProjectMember::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ProjectMember::getId, Comparator.nullsLast(Comparator.naturalOrder())))
            .map(member -> toRosterMemberInfo(memberMap.get(member.getMemberId()), member.getPart(),
                matchedRoundMap.get(ProjectMemberKey.of(projectId, member.getMemberId()))))
            .filter(Objects::nonNull)
            .toList();
        return ProjectMembersResponse.from(new ProjectMatchingRosterInfo(projectId, productOwner, roster));
    }

    private Map<ProjectMemberKey, MatchedRoundInfo> loadMatchedRoundMap(
        Collection<Long> projectIds,
        Collection<Long> memberIds
    ) {
        if (projectIds == null || projectIds.isEmpty() || memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        List<ProjectMemberMatchedRoundInfo> rows = loadProjectMemberPort
            .listMatchedRoundsByProjectIdsAndMemberIds(projectIds, memberIds);
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }

        return rows.stream()
            .collect(Collectors.toMap(
                ProjectMemberKey::from,
                row -> new MatchedRoundInfo(
                    row.matchingRoundId(),
                    row.matchingRoundType(),
                    row.matchingRoundPhase()
                ),
                (left, right) -> left,
                LinkedHashMap::new
            ));
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

    /**
     * PROJECT-001/002 등 비인증 노출 경로에서 사용. 실명·학교명은 요청자 권한과 무관하게 항상 제외한다.
     */
    private MemberBrief toPublicBrief(MemberInfo info) {
        return info == null ? null : MemberBrief.builder()
            .memberId(info.id())
            .nickname(info.nickname())
            .build();
    }

    private RosterMemberInfo toRosterMemberInfo(MemberInfo info, ChallengerPart part, MatchedRoundInfo round) {
        return RosterMemberInfo.of(info, part,
            round == null ? null : round.id(), round == null ? null : round.type(), round == null ? null : round.phase());
    }

    private record ProjectMemberKey(Long projectId, Long memberId) {

        private static ProjectMemberKey of(Long projectId, Long memberId) {
            return new ProjectMemberKey(projectId, memberId);
        }

        private static ProjectMemberKey from(ProjectMemberMatchedRoundInfo info) {
            return new ProjectMemberKey(info.projectId(), info.memberId());
        }
    }
}
