package com.umc.product.project.application.access;

import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.AuthoritySnapshot;
import com.umc.product.member.application.port.in.query.CheckMemberExistenceUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.domain.Project;

import lombok.RequiredArgsConstructor;

/**
 * 프로젝트 지원 통계 조회 권한 규칙. 통계 API와 프로젝트 권한 API의 통계 capability가 함께 사용한다.
 */
@Component
@RequiredArgsConstructor
public class ProjectStatisticsAccessPolicy {

    private final LoadProjectMemberPort loadProjectMemberPort;
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final CheckMemberExistenceUseCase checkMemberExistenceUseCase;
    private final GetChapterUseCase getChapterUseCase;

    public boolean canReadProjectStatistics(Long memberId, Project project) {
        return isProjectPlanMember(memberId, project)
            || isChapterOperator(memberId, project.getChapterId(), project::getGisuId);
    }

    public boolean canReadChapterStatistics(Long memberId, Long chapterId) {
        return isChapterOperator(memberId, chapterId, () -> getChapterUseCase.getGisuIdByChapterId(chapterId));
    }

    private boolean isProjectPlanMember(Long memberId, Project project) {
        return Objects.equals(project.getProductOwnerMemberId(), memberId)
            || loadProjectMemberPort.isActivePlanMember(project.getId(), memberId);
    }

    /**
     * SUPER_ADMIN 은 항상 허용하고, 그 외에는 해당 기수의 중앙운영사무국(총괄단·운영국원·교육국원)과 지부장,
     * 해당 지부 소속 학교 운영진(회장·부회장·교내 파트장·기타 교내 운영진)만 허용한다.
     */
    private boolean isChapterOperator(Long memberId, Long chapterId, Supplier<Long> gisuIdSupplier) {
        if (!checkMemberExistenceUseCase.existsById(memberId)) {
            return false;
        }
        AuthoritySnapshot authority = checkPermissionUseCase.loadSubject(memberId).toAuthoritySnapshot();
        if (authority.isSuperAdmin()) {
            return true;
        }
        Long gisuId = gisuIdSupplier.get();
        if (gisuId == null) {
            return false;
        }
        return authority.isCentralMemberInGisu(gisuId)
            || authority.isChapterPresidentInGisu(gisuId)
            || isSchoolAdminOfChapter(authority, gisuId, chapterId);
    }

    private boolean isSchoolAdminOfChapter(AuthoritySnapshot authority, Long gisuId, Long chapterId) {
        return getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId).stream()
            .filter(chapter -> Objects.equals(chapter.chapterId(), chapterId))
            .flatMap(chapter -> chapter.schools().stream())
            .anyMatch(school -> authority.isSchoolAdminInGisu(gisuId, school.schoolId()));
    }
}
