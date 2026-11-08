package com.umc.product.project.application.access;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.member.application.port.in.query.CheckMemberExistenceUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo.SchoolInfo;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectPmCandidateAccessPolicy {
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final CheckMemberExistenceUseCase checkMemberExistenceUseCase;
    private final GetChapterUseCase getChapterUseCase;

    public Map<Long, String> accessibleSchools(Long memberId, Long gisuId) {
        if (!checkMemberExistenceUseCase.existsById(memberId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
        var authority = checkPermissionUseCase.loadSubject(memberId).toAuthoritySnapshot();
        boolean allChapters = authority.isCentralCoreInGisu(gisuId);
        var chapters = getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId).stream()
            .filter(chapter -> allChapters || authority.isChapterPresidentInGisu(gisuId, chapter.chapterId()))
            .toList();
        if (!allChapters && chapters.isEmpty()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
        return chapters.stream().flatMap(chapter -> chapter.schools().stream())
            .collect(Collectors.toMap(SchoolInfo::schoolId, SchoolInfo::schoolName, (first, second) -> first));
    }
}
