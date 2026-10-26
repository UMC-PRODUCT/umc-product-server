package com.umc.product.project.adapter.out.persistence;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.application.port.out.dto.ProjectMemberMatchedRoundInfo;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectMemberPersistenceAdapter implements LoadProjectMemberPort, SaveProjectMemberPort {

    private final ProjectMemberJpaRepository repository;
    private final EntityManager entityManager;
    private final ProjectMemberQueryRepository queryRepository;

    @Override
    public List<ProjectMember> listPendingByMatchingRoundId(Long matchingRoundId) {
        return repository.findPendingByRoundId(matchingRoundId, ProjectMemberStatus.PENDING_PUBLICATION);
    }

    @Override
    public List<ProjectMember> listPendingRemovalsByMatchingRoundId(Long matchingRoundId) {
        return repository.findPendingRemovalsByRoundId(matchingRoundId);
    }

    @Override
    public List<Long> listAssignedMemberIdsByGisuId(Long gisuId) {
        return queryRepository.listAssignedMemberIdsByGisuId(gisuId);
    }

    @Override
    public List<Long> listAssignedMemberIdsByGisuId(Long gisuId, Long adjustmentRoundId) {
        return queryRepository.listAssignedMemberIdsByGisuId(gisuId, adjustmentRoundId);
    }

    @Override
    public Map<Long, Set<Long>> listAssignedMemberIdsByProjectIds(Collection<Long> projectIds) {
        return queryRepository.listAssignedMemberIdsByProjectIds(projectIds);
    }

    @Override
    public Map<ChallengerPart, Long> countAssignedByProjectIdGroupByPart(Long projectId) {
        return countAssignedByProjectIdsGroupByProjectIdAndPart(List.of(projectId)).getOrDefault(projectId, Map.of());
    }

    @Override
    public Map<ChallengerPart, Long> countAssignedByProjectIdGroupByPart(Long projectId, Long adjustmentRoundId) {
        return queryRepository.countAssignedByProjectIdGroupByPart(projectId, adjustmentRoundId);
    }

    @Override
    public Map<Long, Map<ChallengerPart, Long>> countAssignedByProjectIdsGroupByProjectIdAndPart(Collection<Long> projectIds) {
        return queryRepository.countAssignedByProjectIdsGroupByProjectIdAndPart(projectIds);
    }

    @Override
    public List<ProjectMemberMatchedRoundInfo> listMatchedRoundsByProjectIdsAndMemberIds(
        Collection<Long> projectIds, Collection<Long> memberIds
    ) {
        return queryRepository.listMatchedRoundsByProjectIdsAndMemberIds(projectIds, memberIds);
    }

    @Override
    public ProjectMember save(ProjectMember member) {
        return repository.save(member);
    }

    @Override
    public List<ProjectMember> saveAll(Collection<ProjectMember> members) {
        return repository.saveAll(members);
    }

    @Override
    public void hardDelete(Long projectMemberId) {
        repository.deleteById(projectMemberId);
    }

    @Override
    public void deleteAllByProjectId(Long projectId) {
        repository.deleteAllByProjectId(projectId);
    }

    @Override
    public List<ProjectMember> listByProjectId(Long projectId) {
        return repository.findByProjectIdAndStatusOrderByCreatedAtAscIdAsc(projectId, ProjectMemberStatus.ACTIVE);
    }

    @Override
    public List<ProjectMember> listActiveAndPendingPublicationByProjectId(Long projectId) {
        return repository.findByProjectIdAndStatusInOrderByCreatedAtAscIdAsc(projectId,
            List.of(ProjectMemberStatus.ACTIVE, ProjectMemberStatus.PENDING_PUBLICATION));
    }

    @Override
    public Map<Long, List<ProjectMember>> listByProjectIds(Collection<Long> projectIds) {
        return repository.findByProjectIdInAndStatusOrderByCreatedAtAscIdAsc(projectIds, ProjectMemberStatus.ACTIVE)
            .stream()
            .collect(Collectors.groupingBy(pm -> pm.getProject().getId()));
    }

    @Override
    public List<ProjectMember> listAdjustmentMembersByProjectIds(Collection<Long> projectIds, Collection<Long> adjustmentRoundIds) {
        return queryRepository.listAdjustmentMembersByProjectIds(projectIds, adjustmentRoundIds);
    }

    @Override
    public Map<Long, List<ProjectMember>> listAssignedByProjectIds(Collection<Long> projectIds) {
        return queryRepository.listAssignedByProjectIds(projectIds);
    }

    @Override
    public List<ProjectMember> listByProjectIdAndPart(Long projectId, ChallengerPart part) {
        return repository.findByProjectIdAndPartAndStatusOrderByCreatedAtAscIdAsc(
            projectId, part, ProjectMemberStatus.ACTIVE);
    }

    @Override
    public Map<Long, List<ProjectMember>> listByProjectIdsAndPartGroupedByProjectId(
        Collection<Long> projectIds, ChallengerPart part
    ) {
        return queryRepository.listByProjectIdsAndPartGroupedByProjectId(projectIds, part);
    }

    @Override
    public Map<ChallengerPart, Long> countByProjectIdGroupByPart(Long projectId) {
        List<Object[]> rows = repository.countByProjectIdGroupByPartRaw(projectId, ProjectMemberStatus.ACTIVE);

        Map<ChallengerPart, Long> result = new EnumMap<>(ChallengerPart.class);
        for (Object[] row : rows) {
            result.put((ChallengerPart) row[0], (Long) row[1]);
        }
        return result;
    }

    @Override
    public Map<Long, Map<ChallengerPart, Long>> countByProjectIdsGroupByProjectIdAndPart(
        Collection<Long> projectIds
    ) {
        return queryRepository.countByProjectIdsGroupByProjectIdAndPart(projectIds);
    }

    @Override
    public Optional<ProjectMember> findByProjectIdAndMemberId(Long projectId, Long memberId) {
        return repository.findByProjectIdAndMemberId(projectId, memberId);
    }

    @Override
    public Optional<ProjectMember> findByProjectIdAndMemberIdForUpdate(Long projectId, Long memberId) {
        Optional<ProjectMember> member = repository.findByProjectIdAndMemberIdForUpdate(projectId, memberId);
        member.ifPresent(value -> entityManager.refresh(value, LockModeType.PESSIMISTIC_WRITE));
        return member;
    }

    @Override
    public ProjectMember getByProjectIdAndMemberId(Long projectId, Long memberId) {
        return repository.findByProjectIdAndMemberId(projectId, memberId)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));
    }

    @Override
    public boolean existsByGisuAndMember(Long gisuId, Long memberId) {
        return repository.existsByProject_GisuIdAndMemberIdAndStatusIn(gisuId, memberId,
            List.of(ProjectMemberStatus.ACTIVE, ProjectMemberStatus.PENDING_PUBLICATION));
    }

    @Override
    public boolean existsAssignedMemberExcludingPendingRemoval(Long gisuId, Long memberId, Long adjustmentRoundId) {
        return queryRepository.existsAssignedMemberExcludingPendingRemoval(gisuId, memberId, adjustmentRoundId);
    }

    @Override
    public boolean isActivePlanMember(Long projectId, Long memberId) {
        return repository.existsByProjectIdAndMemberIdAndPartAndStatus(
            projectId, memberId, ChallengerPart.PLAN, ProjectMemberStatus.ACTIVE
        );
    }

    @Override
    public List<Long> listProjectIdsByActivePlanMember(Collection<Long> projectIds, Long memberId) {
        return queryRepository.listProjectIdsByActivePlanMember(projectIds, memberId);
    }

    @Override
    public Optional<ProjectMember> findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
        Long memberId, Long gisuId, MatchingType matchingType
    ) {
        return queryRepository.findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
            memberId, gisuId, matchingType);
    }
}
