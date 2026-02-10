package com.umc.product.authorization.application.service.query;

import com.umc.product.authorization.application.port.in.query.ChallengerRoleInfo;
import com.umc.product.authorization.application.port.in.query.GetMemberRolesUseCase;
import com.umc.product.authorization.application.port.out.LoadChallengerRolePort;
import com.umc.product.authorization.domain.ChallengerRole;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자의 역할 정보를 조회하는 Query Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberRolesQueryService implements GetMemberRolesUseCase {

    private final LoadChallengerRolePort loadChallengerRolePort;

    @Override
    public List<ChallengerRoleInfo> getRoles(Long memberId) {
        return loadChallengerRolePort.findByMemberId(memberId).stream()
            .map(ChallengerRoleInfo::fromEntity)
            .toList();
    }

    @Override
    public List<ChallengerRoleType> getRoleTypes(Long memberId) {
        List<ChallengerRole> roles = loadChallengerRolePort.findByMemberId(memberId);

        // 중복 제거하여 반환
        return roles.stream()
            .map(ChallengerRole::getChallengerRoleType)
            .distinct()
            .toList();
    }

    @Override
    public boolean hasRole(Long memberId, ChallengerRoleType role) {
        if (role == null) {
            return false;
        }

        List<ChallengerRole> roles = loadChallengerRolePort.findByMemberId(memberId);

        return roles.stream()
            .anyMatch(r -> r.getChallengerRoleType() == role);
    }

    @Override
    public boolean hasAnyRole(Long memberId, ChallengerRoleType... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }

        Set<ChallengerRoleType> targetRoles = Arrays.stream(roles).collect(Collectors.toSet());
        List<ChallengerRole> memberRoles = loadChallengerRolePort.findByMemberId(memberId);

        return memberRoles.stream()
            .map(ChallengerRole::getChallengerRoleType)
            .anyMatch(targetRoles::contains);
    }

    @Override
    public boolean hasAllRoles(Long memberId, ChallengerRoleType... roles) {
        if (roles == null || roles.length == 0) {
            return true;
        }

        Set<ChallengerRoleType> targetRoles = Arrays.stream(roles).collect(Collectors.toSet());
        List<ChallengerRole> memberRoles = loadChallengerRolePort.findByMemberId(memberId);

        Set<ChallengerRoleType> memberRoleTypes = memberRoles.stream()
            .map(ChallengerRole::getChallengerRoleType)
            .collect(Collectors.toSet());

        return memberRoleTypes.containsAll(targetRoles);
    }

    @Override
    public boolean isCentralCore(Long memberId) {
        List<ChallengerRole> roles = loadChallengerRolePort.findByMemberId(memberId);

        return roles.stream()
            .map(ChallengerRole::getChallengerRoleType)
            .anyMatch(ChallengerRoleType::isCentralCore);
    }

    @Override
    public boolean isCentralMember(Long memberId) {
        List<ChallengerRole> roles = loadChallengerRolePort.findByMemberId(memberId);

        return roles.stream()
            .map(ChallengerRole::getChallengerRoleType)
            .anyMatch(ChallengerRoleType::isCentralMember);
    }

    @Override
    public boolean isSchoolCore(Long memberId, Long schoolId) {
        if (schoolId == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "schoolId는 null일 수 없습니다.");
        }

        List<ChallengerRole> roles = loadChallengerRolePort.findByMemberId(memberId);

        return roles.stream()
            .filter(role -> role.getOrganizationType() == OrganizationType.SCHOOL)
            .filter(role -> role.getOrganizationId().equals(schoolId))
            .map(ChallengerRole::getChallengerRoleType)
            .anyMatch(ChallengerRoleType::isSchoolCore);
    }

    @Override
    public boolean isSchoolAdmin(Long memberId, Long schoolId) {
        if (schoolId == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "schoolId는 null일 수 없습니다.");
        }

        List<ChallengerRole> roles = loadChallengerRolePort.findByMemberId(memberId);

        return roles.stream()
            .filter(role -> role.getOrganizationType() == OrganizationType.SCHOOL)
            .filter(role -> role.getOrganizationId().equals(schoolId))
            .map(ChallengerRole::getChallengerRoleType)
            .anyMatch(ChallengerRoleType::isSchoolAdmin);
    }

    @Override
    public boolean isChapterPresident(Long memberId, Long chapterId) {
        if (chapterId == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "chapterId는 null일 수 없습니다.");
        }

        List<ChallengerRole> roles = loadChallengerRolePort.findByMemberId(memberId);

        return roles.stream()
            .filter(role -> role.getOrganizationType() == OrganizationType.CHAPTER)
            .filter(role -> role.getOrganizationId().equals(chapterId))
            .map(ChallengerRole::getChallengerRoleType)
            .anyMatch(roleType -> roleType == ChallengerRoleType.CHAPTER_PRESIDENT);
    }

    @Override
    public List<ChallengerRoleType> getRolesByGisu(Long memberId, Long gisuId) {
        if (gisuId == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_INPUT_VALUE, "gisuId는 null일 수 없습니다.");
        }

        List<ChallengerRole> roles = loadChallengerRolePort.findRolesByMemberIdAndGisuId(memberId, gisuId);

        return roles.stream()
            .map(ChallengerRole::getChallengerRoleType)
            .distinct()
            .toList();
    }

    @Override
    public List<ChallengerRoleType> getRolesBySchool(Long memberId, Long schoolId) {
        if (schoolId == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "schoolId는 null일 수 없습니다.");
        }

        List<ChallengerRole> roles = loadChallengerRolePort.findByMemberId(memberId);

        return roles.stream()
            .filter(role -> role.getOrganizationType() == OrganizationType.SCHOOL)
            .filter(role -> role.getOrganizationId().equals(schoolId))
            .map(ChallengerRole::getChallengerRoleType)
            .distinct()
            .toList();
    }

    @Override
    public List<ChallengerRoleType> getRolesByChapter(Long memberId, Long chapterId) {
        if (chapterId == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "chapterId는 null일 수 없습니다.");
        }

        List<ChallengerRole> roles = loadChallengerRolePort.findByMemberId(memberId);

        return roles.stream()
            .filter(role -> role.getOrganizationType() == OrganizationType.CHAPTER)
            .filter(role -> role.getOrganizationId().equals(chapterId))
            .map(ChallengerRole::getChallengerRoleType)
            .distinct()
            .toList();
    }

    @Override
    public boolean hasRoleInGisu(Long memberId, Long gisuId, ChallengerRoleType role) {
        if (gisuId == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "gisuId는 null일 수 없습니다.");
        }
        if (role == null) {
            return false;
        }

        List<ChallengerRole> roles = loadChallengerRolePort.findRolesByMemberIdAndGisuId(memberId, gisuId);

        return roles.stream()
            .anyMatch(r -> r.getChallengerRoleType() == role);
    }

    @Override
    public boolean hasAnyRoleInGisu(Long memberId, Long gisuId, ChallengerRoleType... roles) {
        if (gisuId == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "gisuId는 null일 수 없습니다.");
        }
        if (roles == null || roles.length == 0) {
            return false;
        }

        Set<ChallengerRoleType> targetRoles = Arrays.stream(roles).collect(Collectors.toSet());
        List<ChallengerRole> memberRoles = loadChallengerRolePort.findRolesByMemberIdAndGisuId(memberId, gisuId);

        return memberRoles.stream()
            .map(ChallengerRole::getChallengerRoleType)
            .anyMatch(targetRoles::contains);
    }

    @Override
    public boolean hasAllRolesInGisu(Long memberId, Long gisuId, ChallengerRoleType... roles) {
        if (gisuId == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "gisuId는 null일 수 없습니다.");
        }
        if (roles == null || roles.length == 0) {
            return true;
        }

        Set<ChallengerRoleType> targetRoles = Arrays.stream(roles).collect(Collectors.toSet());
        List<ChallengerRole> memberRoles = loadChallengerRolePort.findRolesByMemberIdAndGisuId(memberId, gisuId);

        Set<ChallengerRoleType> memberRoleTypes = memberRoles.stream()
            .map(ChallengerRole::getChallengerRoleType)
            .collect(Collectors.toSet());

        return memberRoleTypes.containsAll(targetRoles);
    }

    @Override
    public boolean isCentralCoreInGisu(Long memberId, Long gisuId) {
        if (gisuId == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "gisuId는 null일 수 없습니다.");
        }

        List<ChallengerRole> roles = loadChallengerRolePort.findRolesByMemberIdAndGisuId(memberId, gisuId);

        return roles.stream()
            .map(ChallengerRole::getChallengerRoleType)
            .anyMatch(ChallengerRoleType::isCentralCore);
    }

    @Override
    public Map<Long, List<ChallengerRoleType>> getRoleTypesByChallengerIds(Set<Long> challengerIds) {
        if (challengerIds == null || challengerIds.isEmpty()) {
            return Map.of();
        }

        return loadChallengerRolePort.findByChallengerIdIn(challengerIds).stream()
            .collect(Collectors.groupingBy(
                ChallengerRole::getChallengerId,
                Collectors.mapping(ChallengerRole::getChallengerRoleType, Collectors.toList())
            ));
    }

    @Override
    public boolean isCentralMemberInGisu(Long memberId, Long gisuId) {
        if (gisuId == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "gisuId는 null일 수 없습니다.");
        }

        List<ChallengerRole> roles = loadChallengerRolePort.findRolesByMemberIdAndGisuId(memberId, gisuId);

        return roles.stream()
            .map(ChallengerRole::getChallengerRoleType)
            .anyMatch(ChallengerRoleType::isCentralMember);
    }
}
