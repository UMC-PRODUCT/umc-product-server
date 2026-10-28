package com.umc.product.project.application.service.policy;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.authorization.domain.AuthoritySnapshot;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectManualMatchingAccessPolicy {
    private final CheckChallengerAuthorityUseCase authorityUseCase;

    /** 이미 조회한 권한 정보로 프로젝트의 조정 가능 여부를 판단한다. */
    public static boolean canManage(AuthoritySnapshot authority, Project project) {
        return authority.isCentralCoreInGisu(project.getGisuId())
            || authority.isChapterPresidentInGisu(project.getGisuId(), project.getChapterId());
    }

    public void validate(Long requesterMemberId, Project project) {
        validateChapter(requesterMemberId, project.getGisuId(), project.getChapterId());
    }

    /** 같은 기수·지부의 권한은 한 번만 검증한다. */
    public void validateAll(Long requesterMemberId, Collection<Project> projects) {
        projects.stream()
            .map(project -> new ChapterScope(project.getGisuId(), project.getChapterId()))
            .distinct()
            .forEach(scope -> validateChapter(requesterMemberId, scope.gisuId(), scope.chapterId()));
    }

    /** 지부 단위로 조정 권한을 검증한다. */
    public void validateChapter(Long requesterMemberId, Long gisuId, Long chapterId) {
        boolean allowed = authorityUseCase.isCentralCoreInGisu(requesterMemberId, gisuId)
            || authorityUseCase.isChapterPresidentInGisu(requesterMemberId, gisuId, chapterId);
        if (!allowed) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
    }

    private record ChapterScope(Long gisuId, Long chapterId) {
    }
}
