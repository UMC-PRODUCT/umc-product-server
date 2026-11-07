package com.umc.product.project.application.service.query;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.project.application.access.ProjectPmCandidateAccessPolicy;
import com.umc.product.project.application.port.in.query.SearchProjectPmCandidateUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectPmCandidateInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectPmCandidateQueryService implements SearchProjectPmCandidateUseCase {
    private final ProjectPmCandidateAccessPolicy accessPolicy;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public Page<ProjectPmCandidateInfo> search(Long requesterMemberId, Long gisuId, String keyword, Pageable pageable) {
        getGisuUseCase.getById(gisuId);
        var schools = accessPolicy.accessibleSchools(requesterMemberId, gisuId);
        if (schools.isEmpty()) {
            return Page.empty(pageable);
        }
        var challengers = getChallengerUseCase.listBasicByGisuId(gisuId).stream()
            .filter(challenger -> Objects.equals(challenger.gisuId(), gisuId))
            .filter(challenger -> challenger.part() == ChallengerPart.PLAN
                && challenger.challengerStatus() == ChallengerStatus.ACTIVE).toList();
        var memberIds = challengers.stream().map(challenger -> challenger.memberId()).collect(Collectors.toSet());
        var members = getMemberUseCase.findAllBasicByIds(memberIds);
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<ProjectPmCandidateInfo> candidates = challengers.stream()
            .filter(challenger -> members.containsKey(challenger.memberId()))
            .filter(challenger -> members.get(challenger.memberId()).status() == MemberStatus.ACTIVE)
            .filter(challenger -> schools.containsKey(members.get(challenger.memberId()).schoolId()))
            .map(challenger -> {
                var member = members.get(challenger.memberId());
                return new ProjectPmCandidateInfo(member.memberId(), challenger.challengerId(), member.name(),
                    member.nickname(), member.schoolId(), schools.get(member.schoolId()));
            })
            .filter(candidate -> matches(candidate.name(), normalizedKeyword)
                || matches(candidate.nickname(), normalizedKeyword))
            .sorted(Comparator.comparing(ProjectPmCandidateInfo::name, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ProjectPmCandidateInfo::memberId)).toList();
        int start = pageable.isPaged() ? (int) Math.min(pageable.getOffset(), candidates.size()) : 0;
        int end = pageable.isPaged()
            ? (int) Math.min((long) start + pageable.getPageSize(), candidates.size()) : candidates.size();
        return new PageImpl<>(candidates.subList(start, end), pageable, candidates.size());
    }

    private boolean matches(String value, String keyword) {
        return keyword.isEmpty() || (value != null && value.toLowerCase(Locale.ROOT).contains(keyword));
    }
}
