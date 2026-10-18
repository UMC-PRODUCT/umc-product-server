package com.umc.product.project.application.service.query;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.project.application.port.in.query.GetMyMatchingAffiliationUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingAffiliationInfo;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.domain.ProjectMatchingChapterTransfer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMatchingAffiliationQueryService implements GetMyMatchingAffiliationUseCase {

    private final ProjectMatchingChapterResolver matchingChapterResolver;
    private final GetChapterUseCase getChapterUseCase;
    private final GetGisuUseCase getGisuUseCase;

    @Override
    public ProjectMatchingAffiliationInfo get(Long memberId, Long gisuId) {
        Long targetGisuId = gisuId != null ? gisuId : getGisuUseCase.getActiveGisuId();

        ChapterInfo originChapter = matchingChapterResolver.findOriginChapter(memberId, targetGisuId)
            .orElse(null);
        Optional<ProjectMatchingChapterTransfer> transfer =
            matchingChapterResolver.findActiveTransfer(memberId, targetGisuId);
        ChapterInfo matchingChapter = transfer
            .map(active -> getChapterUseCase.getChapterById(active.getToChapterId()))
            .orElse(originChapter);

        return new ProjectMatchingAffiliationInfo(targetGisuId, originChapter, matchingChapter, transfer.isPresent());
    }
}
