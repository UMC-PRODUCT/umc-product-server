package com.umc.product.project.application.access;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
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
        boolean core = authority.isCentralCoreInGisu(gisuId);
        List<ChapterWithSchoolsInfo> chapters = getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId).stream()
            .filter(chapter -> core || authority.isChapterPresidentInGisu(gisuId, chapter.chapterId()))
            .sorted(Comparator.comparing(ChapterWithSchoolsInfo::chapterId)).toList();
        if (!core && chapters.isEmpty()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
        return chapters;
    }

    public ChapterWithSchoolsInfo requireChapter(Long memberId, Long gisuId, Long chapterId) {
        return accessibleChapters(memberId, gisuId).stream()
            .filter(chapter -> Objects.equals(chapter.chapterId(), chapterId)).findFirst()
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED));
    }
}
