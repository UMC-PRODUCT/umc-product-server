package com.umc.product.project.application.service.query;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.application.port.in.query.SearchProjectMatchingCandidateUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingCandidateInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.service.policy.ProjectManualMatchingAccessPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMatchingCandidateQueryService implements SearchProjectMatchingCandidateUseCase {
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final ProjectMatchingChapterResolver matchingChapterResolver;
    private final ProjectManualMatchingAccessPolicy accessPolicy;

    @Override
    public Page<ProjectMatchingCandidateInfo> search(Long projectId, Long requesterMemberId, ChallengerPart part,
        String keyword, Pageable pageable) {
        var project = loadProjectPort.getById(projectId);
        accessPolicy.validate(requesterMemberId, project);
        project.validateApplicable();
        if (part == null || part == ChallengerPart.PLAN || part == ChallengerPart.ADMIN) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_PART_NOT_ALLOWED);
        }
        Set<Long> assigned = new HashSet<>(loadProjectMemberPort.listAssignedMemberIdsByGisuId(project.getGisuId()));
        assigned.addAll(loadProjectApplicationPort.listApprovedApplicantMemberIdsByGisuId(project.getGisuId()));
        assigned.add(project.getProductOwnerMemberId());
        var challengers = matchingChapterResolver.deltaOf(project.getChapterId())
            .apply(getChallengerUseCase.listByChapterId(project.getChapterId())).stream()
            .filter(c -> c.challengerStatus() == ChallengerStatus.ACTIVE && c.part() == part)
            .filter(c -> !assigned.contains(c.memberId())).toList();
        Set<Long> memberIds = challengers.stream().map(c -> c.memberId()).collect(Collectors.toSet());
        var members = memberIds.isEmpty() ? java.util.Map.<Long, MemberInfo>of()
            : getMemberUseCase.findAllByIds(memberIds);
        List<ProjectMatchingCandidateInfo> candidates = challengers.stream()
            .filter(c -> members.containsKey(c.memberId()))
            .map(c -> {
                MemberInfo member = members.get(c.memberId());
                return new ProjectMatchingCandidateInfo(c.memberId(), c.challengerId(), member.name(),
                    member.nickname(),
                    member.schoolId(), member.schoolName(), c.part());
            })
            .filter(c -> matches(c.name(), keyword) || matches(c.nickname(), keyword))
            .sorted(Comparator.comparing(ProjectMatchingCandidateInfo::memberId)).toList();
        int start = pageable.isPaged() ? (int) Math.min(pageable.getOffset(), candidates.size()) : 0;
        int end = pageable.isPaged() ? (int) Math.min((long) start + pageable.getPageSize(), candidates.size())
            : candidates.size();
        return new PageImpl<>(candidates.subList(start, end), pageable, candidates.size());
    }

    private boolean matches(String value, String keyword) {
        return keyword == null || keyword.isBlank() || (value != null && value.contains(keyword.trim()));
    }
}
