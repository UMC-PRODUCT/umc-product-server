package com.umc.product.project.application.access;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.project.application.access.ProjectAccessScope.All;
import com.umc.product.project.application.access.ProjectAccessScope.ChapterScoped;
import com.umc.product.project.application.access.ProjectAccessScope.None;
import com.umc.product.project.application.access.ProjectAccessScope.OwnerOnly;
import com.umc.product.project.application.access.ProjectAccessScope.PublicOnly;
import com.umc.product.project.application.access.ProjectAccessScope.SchoolScoped;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 호출 컨텍스트(공개 검색 vs 관리 화면) + 사용자 역할에 따라 {@link ProjectAccessScope} 를 결정한다.
 * <p>
 * 같은 사용자라도 호출 의도에 따라 결과가 달라야 하므로 {@code resolveForPublicSearch} /
 * {@code resolveForManagement} 두 메서드로 명시적으로 분기한다.
 */
@Component
@RequiredArgsConstructor
public class ProjectAccessScopeResolver {

    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final LoadProjectPort loadProjectPort;

    /**
     * 공개 검색(PROJECT-001) 컨텍스트.
     * <p>
     * 운영진(지부장/학교 회장단)도 일반 챌린저와 동일하게 IN_PROGRESS 만 노출. Central Core 만 전체 노출.
     */
    public ProjectAccessScope resolveForPublicSearch(
        Long memberId, Long gisuId, Set<ProjectStatus> requestedStatuses
    ) {
        if (getChallengerRoleUseCase.isCentralCoreInGisu(memberId, gisuId)) {
            return new All(requestedStatuses);
        }
        return new PublicOnly();
    }

    /**
     * 관리 화면(PROJECT-006) 컨텍스트. 역할별 scope 차등 적용.
     * <ol>
     *   <li>Central Core → 전체 (요청 상태 그대로)</li>
     *   <li>지부장 → 본인 지부</li>
     *   <li>학교 회장단 → 본인 학교</li>
     *   <li>PM 챌린저 → 본인이 owner 인 프로젝트만</li>
     *   <li>그 외 → 관리 대상 0건</li>
     * </ol>
     */
    public ProjectAccessScope resolveForManagement(
        Long memberId, Long gisuId, Set<ProjectStatus> requestedStatuses
    ) {
        List<ChallengerRoleInfo> rolesInGisu = getChallengerRoleUseCase.findAllByMemberId(memberId).stream()
            .filter(role -> Objects.equals(role.gisuId(), gisuId))
            .toList();

        if (rolesInGisu.stream().anyMatch(r -> r.roleType().isAtLeastCentralCore())) {
            return new All(requestedStatuses);
        }

        Optional<Long> chapterId = chapterPresidentOrgId(rolesInGisu);
        if (chapterId.isPresent()) {
            return new ChapterScoped(chapterId.get(), requestedStatuses);
        }

        Optional<Long> schoolId = schoolCoreOrgId(rolesInGisu);
        if (schoolId.isPresent()) {
            return new SchoolScoped(schoolId.get(), requestedStatuses);
        }

        if (loadProjectPort.existsByOwnerAndGisu(memberId, gisuId)) {
            return new OwnerOnly(memberId, requestedStatuses);
        }

        return new None();
    }

    private Optional<Long> chapterPresidentOrgId(List<ChallengerRoleInfo> rolesInGisu) {
        return rolesInGisu.stream()
            .filter(role -> role.roleType() == ChallengerRoleType.CHAPTER_PRESIDENT)
            .map(ChallengerRoleInfo::organizationId)
            .findFirst();
    }

    private Optional<Long> schoolCoreOrgId(List<ChallengerRoleInfo> rolesInGisu) {
        return rolesInGisu.stream()
            .filter(role -> role.roleType() == ChallengerRoleType.SCHOOL_PRESIDENT
                || role.roleType() == ChallengerRoleType.SCHOOL_VICE_PRESIDENT)
            .map(ChallengerRoleInfo::organizationId)
            .findFirst();
    }
}
