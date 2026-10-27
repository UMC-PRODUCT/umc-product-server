package com.umc.product.project.application.service.policy;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.domain.Project;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectStatisticsAccessPolicy {

    private final LoadProjectMemberPort loadProjectMemberPort;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetChapterUseCase getChapterUseCase;

    public boolean canReadProjectStatistics(Long memberId, Project project) {
        if (Objects.equals(project.getProductOwnerMemberId(), memberId)) {
            return true;
        }
        if (loadProjectMemberPort.isActivePlanMember(project.getId(), memberId)) {
            return true;
        }
        return canReadChapterStatistics(memberId, project.getChapterId(), project::getGisuId);
    }

    public boolean canReadChapterStatistics(Long memberId, Long chapterId) {
        return canReadChapterStatistics(memberId, chapterId,
            () -> getChapterUseCase.getGisuIdByChapterId(chapterId));
    }

    /**
     * 지부 운영진 통계 조회 권한을 판정한다. SUPER_ADMIN 은 항상 허용하고, 그 외에는 해당 기수의 총괄단·해당 지부장·
     * 해당 지부 소속 학교 회장단만 허용한다.
     * <p>
     * 운영진 역할이 없는 회원은 기수 조회 없이 바로 거부한다. 기수는 지부에서 조회해야 하는 경우가 있어 {@link Supplier}로 지연 조회한다.
     */
    private boolean canReadChapterStatistics(Long memberId, Long chapterId, Supplier<Long> gisuIdSupplier) {
        if (getChallengerRoleUseCase.isSuperAdmin(memberId)) {
            return true;
        }
        List<ChallengerRoleInfo> staffRoles = getChallengerRoleUseCase.findAllByMemberId(memberId).stream()
            .filter(role -> role.roleType().isAtLeastCentralCore()
                || role.roleType() == ChallengerRoleType.CHAPTER_PRESIDENT
                || role.roleType().isAtLeastSchoolCore())
            .toList();
        if (staffRoles.isEmpty()) {
            return false;
        }
        Long gisuId = gisuIdSupplier.get();
        if (gisuId == null) {
            return false;
        }
        List<ChallengerRoleInfo> roles = staffRoles.stream()
            .filter(role -> Objects.equals(role.gisuId(), gisuId))
            .toList();
        return roles.stream().anyMatch(role -> role.roleType().isAtLeastCentralCore()
                || (role.roleType() == ChallengerRoleType.CHAPTER_PRESIDENT
                    && Objects.equals(role.organizationId(), chapterId)))
            || isSchoolCoreOfChapter(roles, chapterId, gisuId);
    }

    private boolean isSchoolCoreOfChapter(List<ChallengerRoleInfo> roles, Long chapterId, Long gisuId) {
        Set<Long> schoolIds = roles.stream()
            .filter(role -> role.roleType().isAtLeastSchoolCore())
            .map(ChallengerRoleInfo::organizationId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (schoolIds.isEmpty()) {
            return false;
        }
        return getChapterUseCase.getChapterMapByGisuIdsAndSchoolIds(Set.of(gisuId), schoolIds)
            .getOrDefault(gisuId, Map.of()).values().stream()
            .anyMatch(chapter -> Objects.equals(chapter.id(), chapterId));
    }
}
