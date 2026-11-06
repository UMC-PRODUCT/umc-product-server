package com.umc.product.project.application.access;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.AuthoritySnapshot;
import com.umc.product.member.application.port.in.query.CheckMemberExistenceUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectQuotaAccessPolicy {
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final CheckMemberExistenceUseCase checkMemberExistenceUseCase;
    private final GetChapterUseCase getChapterUseCase;

    public List<ChapterWithSchoolsInfo> accessibleChapters(Long memberId, Long gisuId) {
        if (!checkMemberExistenceUseCase.existsById(memberId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
        var authority = checkPermissionUseCase.loadSubject(memberId).toAuthoritySnapshot();
        List<ChapterWithSchoolsInfo> chapters = getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId);
        boolean canView = authority.isCentralMemberInGisu(gisuId) || chapters.stream()
            .anyMatch(chapter -> authority.isChapterPresidentInGisu(gisuId, chapter.chapterId()));
        if (!canView) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
        return chapters.stream().sorted(Comparator
            .comparing((ChapterWithSchoolsInfo chapter) -> !authority.isChapterPresidentInGisu(gisuId, chapter.chapterId()))
            .thenComparing(ChapterWithSchoolsInfo::chapterId)).toList();
    }

    public boolean canManage(AuthoritySnapshot authority, Long gisuId, Long chapterId) {
        return authority.isCentralCoreInGisu(gisuId)
            || authority.isChapterPresidentInGisu(gisuId, chapterId);
    }

    public boolean canManage(Long memberId, Long gisuId, Long chapterId) {
        return checkMemberExistenceUseCase.existsById(memberId)
            && canManage(checkPermissionUseCase.loadSubject(memberId).toAuthoritySnapshot(), gisuId, chapterId);
    }

    public void requireManageChapter(Long memberId, Long gisuId, Long chapterId) {
        if (!canManage(memberId, gisuId, chapterId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
        requireChapter(memberId, gisuId, chapterId);
    }

    public ChapterWithSchoolsInfo requireChapter(Long memberId, Long gisuId, Long chapterId) {
        return accessibleChapters(memberId, gisuId).stream()
            .filter(chapter -> Objects.equals(chapter.chapterId(), chapterId)).findFirst()
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED));
    }
}
