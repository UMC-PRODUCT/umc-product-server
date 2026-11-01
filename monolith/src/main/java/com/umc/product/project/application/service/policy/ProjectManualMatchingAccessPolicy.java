package com.umc.product.project.application.service.policy;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectManualMatchingAccessPolicy {
    private final CheckChallengerAuthorityUseCase authorityUseCase;

    /** 해당 기수의 총괄단 또는 해당 지부장인지 판단한다. */
    public boolean canManage(Long requesterMemberId, Long gisuId, Long chapterId) {
        return authorityUseCase.isCentralCoreInGisu(requesterMemberId, gisuId)
            || authorityUseCase.isChapterPresidentInGisu(requesterMemberId, gisuId, chapterId);
    }

    public void validate(Long requesterMemberId, Project project) {
        validateChapter(requesterMemberId, project.getGisuId(), project.getChapterId());
    }

    /** 지부 단위로 조정 권한을 검증한다. */
    public void validateChapter(Long requesterMemberId, Long gisuId, Long chapterId) {
        if (!canManage(requesterMemberId, gisuId, chapterId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
    }
}
