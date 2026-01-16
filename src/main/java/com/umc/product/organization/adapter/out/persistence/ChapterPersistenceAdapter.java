package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.application.port.out.command.ManageChapterPort;
import com.umc.product.organization.application.port.out.query.LoadChapterPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.exception.OrganizationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChapterPersistenceAdapter implements LoadChapterPort, ManageChapterPort {

    private final ChapterJpaRepository chapterJpaRepository;

    public void existsById(Long chapterId) {
        if(!chapterJpaRepository.existsById(chapterId)) {
            throw new BusinessException(Domain.ORGANIZATION, OrganizationErrorCode.CHAPTER_NOT_FOUND);
        }
    }

    public Chapter findById(Long chapterId) {
        return chapterJpaRepository.findById(chapterId).orElseThrow(() ->
                new BusinessException(Domain.ORGANIZATION, OrganizationErrorCode.CHAPTER_NOT_FOUND));
    }

    public void save(Chapter chapter) {
        chapterJpaRepository.save(chapter);
    }

}
