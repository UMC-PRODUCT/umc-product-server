package com.umc.product.notice.application.service.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.query.ListChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeAuthorInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NoticeAuthorQueryService {

    private final GetGisuUseCase getGisuUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final ListChallengerRoleUseCase listChallengerRoleUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final GetSchoolUseCase getSchoolUseCase;

    public Map<Long, NoticeAuthorInfo> getAll(Map<Long, MemberInfo> members) {
        if (members.isEmpty()) {
            return Map.of();
        }
        Long activeGisuId = getGisuUseCase.findActiveGisu().map(gisu -> gisu.gisuId()).orElse(null);
        Map<Long, ChallengerRoleInfo> highestRoleByMemberId = loadHighestRolesByMemberId(members.keySet(), activeGisuId);
        Map<Long, String> schoolNameById = loadSchoolNames(highestRoleByMemberId.values());
        Map<Long, String> chapterNameById = loadChapterNames(highestRoleByMemberId.values(), activeGisuId);

        Map<Long, NoticeAuthorInfo> authorByMemberId = new HashMap<>();
        members.forEach((memberId, member) -> authorByMemberId.put(memberId,
            toAuthorInfo(memberId, member, activeGisuId, highestRoleByMemberId.get(memberId), schoolNameById, chapterNameById)));
        return authorByMemberId;
    }

    private Map<Long, ChallengerRoleInfo> loadHighestRolesByMemberId(Set<Long> memberIds, Long gisuId) {
        if (gisuId == null) {
            return Map.of();
        }
        Map<Long, Long> memberIdByChallengerId = getChallengerUseCase.listBasicByMemberIdsAndGisuId(memberIds, gisuId)
            .stream().collect(Collectors.toMap(ChallengerBasicInfo::challengerId, ChallengerBasicInfo::memberId));
        if (memberIdByChallengerId.isEmpty()) {
            return Map.of();
        }

        List<ChallengerRoleInfo> roles = listChallengerRoleUseCase
            .listByChallengerIdsAndGisuId(memberIdByChallengerId.keySet(), gisuId);
        Map<Long, ChallengerRoleInfo> highestRoleByMemberId = new HashMap<>();
        for (ChallengerRoleInfo role : roles) {
            Long memberId = memberIdByChallengerId.get(role.challengerId());
            highestRoleByMemberId.merge(memberId, role, this::higherRole);
        }
        return highestRoleByMemberId;
    }

    private ChallengerRoleInfo higherRole(ChallengerRoleInfo current, ChallengerRoleInfo candidate) {
        // 기존 역할 enum의 선언 순서가 직책 우선순위다. 같은 직책이면 먼저 조회된 역할을 유지한다.
        return current.roleType().compareTo(candidate.roleType()) <= 0 ? current : candidate;
    }

    private Map<Long, String> loadSchoolNames(Collection<ChallengerRoleInfo> roles) {
        Set<Long> schoolIds = roles.stream()
            .filter(role -> role.organizationType() == OrganizationType.SCHOOL)
            .map(ChallengerRoleInfo::organizationId).collect(Collectors.toSet());
        if (schoolIds.isEmpty()) {
            return Map.of();
        }
        return getSchoolUseCase.listDetailsByIds(schoolIds).stream()
            .collect(Collectors.toMap(school -> school.schoolId(), school -> school.schoolName()));
    }

    private Map<Long, String> loadChapterNames(Collection<ChallengerRoleInfo> roles, Long gisuId) {
        if (roles.stream().noneMatch(role -> role.organizationType() == OrganizationType.CHAPTER)) {
            return Map.of();
        }
        return getChapterUseCase.listByGisuId(gisuId).stream()
            .collect(Collectors.toMap(chapter -> chapter.id(), chapter -> chapter.name()));
    }

    private NoticeAuthorInfo toAuthorInfo(Long memberId, MemberInfo member, Long gisuId, ChallengerRoleInfo role,
                                        Map<Long, String> schoolNameById, Map<Long, String> chapterNameById) {
        if (role == null) {
            return new NoticeAuthorInfo(memberId, member.name(), member.nickname(), gisuId,
                null, null, null, null, null);
        }
        String organizationName = switch (role.organizationType()) {
            case CENTRAL -> "UMC 중앙";
            case CHAPTER -> chapterNameById.get(role.organizationId());
            case SCHOOL -> schoolNameById.get(role.organizationId());
            default -> null;
        };
        return new NoticeAuthorInfo(memberId, member.name(), member.nickname(), gisuId,
            role.roleType(), roleName(role.roleType()), role.organizationType(), role.organizationId(), organizationName);
    }

    private String roleName(ChallengerRoleType role) {
        return switch (role) {
            case CENTRAL_PRESIDENT -> "총괄";
            case CENTRAL_VICE_PRESIDENT -> "부총괄";
            case CENTRAL_OPERATING_TEAM_MEMBER -> "중앙 운영국";
            case CENTRAL_EDUCATION_TEAM_MEMBER -> "중앙 교육국";
            case CHAPTER_PRESIDENT -> "지부장";
            case SCHOOL_PRESIDENT -> "회장";
            case SCHOOL_VICE_PRESIDENT -> "부회장";
            case SCHOOL_PART_LEADER -> "파트장";
            case SCHOOL_ETC_ADMIN -> "교내 운영진";
        };
    }
}
