package com.umc.product.project.application.service.policy;

import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.AuthoritySnapshot;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.member.application.port.in.query.CheckMemberExistenceUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.domain.Project;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectStatisticsAccessPolicy {

    private final LoadProjectMemberPort loadProjectMemberPort;
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final CheckMemberExistenceUseCase checkMemberExistenceUseCase;
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
        if (!checkMemberExistenceUseCase.existsById(memberId)) {
            return false;
        }
        var authority = checkPermissionUseCase.loadSubject(memberId).toAuthoritySnapshot();
        if (authority.isSuperAdmin()) {
            return true;
        }
        if (!hasStatisticsAuthority(authority)) {
            return false;
        }
        Long gisuId = gisuIdSupplier.get();
        if (gisuId == null) {
            return false;
        }
        return authority.isCentralCoreInGisu(gisuId)
            || authority.isChapterPresidentInGisu(gisuId, chapterId)
            || isSchoolCoreOfChapter(authority, chapterId, gisuId);
    }

    private boolean hasStatisticsAuthority(AuthoritySnapshot authority) {
        return authority.isCentralCoreInAnyGisu()
            || authority.challengerRoles().stream()
                .map(RoleAttribute::organizationId)
                .filter(Objects::nonNull)
                .distinct()
                .anyMatch(organizationId -> authority.isChapterPresidentInAnyGisu(organizationId)
                    || authority.isSchoolCoreInAnyGisu(organizationId));
    }

    private boolean isSchoolCoreOfChapter(AuthoritySnapshot authority, Long chapterId, Long gisuId) {
        return getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId).stream()
            .filter(chapter -> Objects.equals(chapter.chapterId(), chapterId))
            .flatMap(chapter -> chapter.schools().stream())
            .anyMatch(school -> authority.isSchoolCoreInGisu(gisuId, school.schoolId()));
    }
}
