package com.umc.product.project.application.service.policy;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;

@Component
public class ProjectDraftQuotaPolicy {
    /** 직접 등록하는 운영진만 본인 초안의 공개 준비에 필요한 TO를 설정할 수 있다. */
    public boolean canEdit(Project project, SubjectAttributes subject) {
        if (project.getStatus() != ProjectStatus.DRAFT
            || !Objects.equals(project.getCreatorMemberId(), subject.memberId())) {
            return false;
        }
        var authority = subject.toAuthoritySnapshot();
        return authority.isCentralCoreInGisu(project.getGisuId())
            || authority.isChapterPresidentInGisu(project.getGisuId(), project.getChapterId());
    }
}
