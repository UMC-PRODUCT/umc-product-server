package com.umc.product.project.adapter.in.web.assembler;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.response.PageResponse;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.response.DraftProjectResponse;
import com.umc.product.project.adapter.in.web.dto.response.ManagedProjectSummaryResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectDetailResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMembersResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMembersResponse.PartGroup;
import com.umc.product.project.adapter.in.web.dto.response.ProjectSummaryResponse;
import com.umc.product.project.application.port.in.query.GetProjectUseCase;
import com.umc.product.project.application.port.in.query.SearchManagedProjectUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.in.query.dto.SearchManagedProjectQuery;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMember;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final SearchManagedProjectUseCase searchManagedProjectUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    private final LoadProjectMemberPort loadProjectMemberPort;

    /**
     * PROJECT-001 프로젝트 목록 조회.
     * <p>
     * 마스킹: 본인이 PM 인 row 는 실명, Central Core 는 전체 실명, 그 외는 마스킹.
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

        boolean isCentralCore = getChallengerRoleUseCase.isCentralCoreInGisu(requesterMemberId, query.gisuId());

        return PageResponse.of(page, info -> {
            MemberBrief owner = toBrief(memberMap.get(info.productOwnerMemberId()));
            ProjectSummaryResponse response = ProjectSummaryResponse.from(info, owner);
            boolean canSeeFullInfo = isCentralCore
                || Objects.equals(requesterMemberId, info.productOwnerMemberId());
            return canSeeFullInfo ? response : response.toPublic();
        });
    }

    /**
     * PROJECT-002 프로젝트 상세 조회.
     */
    public ProjectDetailResponse detailFor(Long projectId, Long requesterMemberId) {
        ProjectInfo info = getProjectUseCase.getById(projectId);

        Map<Long, MemberInfo> memberMap = loadMembers(info);

        MemberBrief owner = toBrief(memberMap.get(info.productOwnerMemberId()));
        List<MemberBrief> coOwners = info.coProductOwnerMemberIds().stream()
            .map(id -> toBrief(memberMap.get(id)))
            .toList();

        ProjectDetailResponse response =
            ProjectDetailResponse.from(info, owner, coOwners, resolveApplicationFormId(projectId));

        boolean canSeeFullInfo = Objects.equals(requesterMemberId, info.productOwnerMemberId())
            || getChallengerRoleUseCase.isCentralCoreInGisu(requesterMemberId, info.gisuId());
        return canSeeFullInfo ? response : response.toPublic();
    }

    /**
     * PROJECT-006 관리 화면 프로젝트 목록.
     * <p>
     * 호출자 역할에 따라 자동 scope 적용. 본인이 PM 인 row 또는 Central Core 호출 시 실명 노출, 그 외 마스킹.
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

        boolean isCentralCore = getChallengerRoleUseCase.isCentralCoreInGisu(
            requesterMemberId, query.gisuId());

        return PageResponse.of(page, info -> {
            MemberBrief owner = toBrief(memberMap.get(info.productOwnerMemberId()));
            ManagedProjectSummaryResponse response = ManagedProjectSummaryResponse.from(info, owner);
            boolean canSeeFullInfo = isCentralCore
                || Objects.equals(requesterMemberId, info.productOwnerMemberId());
            return canSeeFullInfo ? response : response.toPublic();
        });
    }

    /**
     * PROJECT-003 프로젝트 팀원 구성 조회.
     * <p>
     * 메인 PM 별도 노출 + 보조 PM(PLAN 파트의 다른 멤버) + 그 외 파트별 그룹. 각 그룹은 닉네임 가나다순 정렬.
     */
    public ProjectMembersResponse membersFor(Long projectId, Long requesterMemberId) {
        ProjectInfo info = getProjectUseCase.getById(projectId);
        List<ProjectMember> members = loadProjectMemberPort.listByProjectId(projectId);

        Set<Long> memberIds = members.stream().map(ProjectMember::getMemberId).collect(Collectors.toSet());
        memberIds.add(info.productOwnerMemberId());
        Map<Long, MemberInfo> memberMap = memberIds.isEmpty()
            ? Map.of()
            : getMemberUseCase.findAllByIds(memberIds);

        MemberBrief productOwner = toBrief(memberMap.get(info.productOwnerMemberId()));

        Map<ChallengerPart, List<MemberBrief>> partToMembers = new EnumMap<>(ChallengerPart.class);
        for (ProjectMember m : members) {
            MemberBrief brief = toBrief(memberMap.get(m.getMemberId()));
            if (brief == null) continue;
            partToMembers.computeIfAbsent(m.getPart(), p -> new java.util.ArrayList<>()).add(brief);
        }

        Comparator<MemberBrief> byNickname = Comparator.comparing(
            MemberBrief::nickname, Comparator.nullsLast(Comparator.naturalOrder()));

        List<MemberBrief> coProductOwners = partToMembers.getOrDefault(ChallengerPart.PLAN, List.of()).stream()
            .filter(b -> !Objects.equals(b.memberId(), info.productOwnerMemberId()))
            .sorted(byNickname)
            .toList();

        List<PartGroup> partGroups = java.util.Arrays.stream(ChallengerPart.values())
            .filter(p -> p != ChallengerPart.PLAN)
            .map(p -> new PartGroup(p,
                partToMembers.getOrDefault(p, List.of()).stream().sorted(byNickname).toList()))
            .filter(g -> !g.members().isEmpty())
            .toList();

        ProjectMembersResponse response = ProjectMembersResponse.builder()
            .projectId(projectId)
            .productOwner(productOwner)
            .coProductOwners(coProductOwners)
            .partGroups(partGroups)
            .build();

        boolean canSeeFullInfo = Objects.equals(requesterMemberId, info.productOwnerMemberId())
            || getChallengerRoleUseCase.isCentralCoreInGisu(requesterMemberId, info.gisuId());
        return canSeeFullInfo ? response : response.toPublic();
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
