package com.umc.product.techblog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.techblog.application.port.in.command.ToggleTechBlogContentLikeUseCase;
import com.umc.product.techblog.application.port.in.query.GetTechBlogContentLikeUseCase;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogLikeInfo;
import com.umc.product.techblog.application.port.out.LoadTechBlogContentPort;
import com.umc.product.techblog.application.port.out.LoadTechBlogLikePort;
import com.umc.product.techblog.application.port.out.SaveTechBlogContentPort;
import com.umc.product.techblog.application.port.out.SaveTechBlogLikePort;
import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentLike;
import com.umc.product.techblog.domain.TechBlogContentType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TechBlogContentReactionService implements GetTechBlogContentLikeUseCase, ToggleTechBlogContentLikeUseCase {

    private final LoadTechBlogContentPort loadTechBlogContentPort;
    private final SaveTechBlogContentPort saveTechBlogContentPort;
    private final LoadTechBlogLikePort loadTechBlogLikePort;
    private final SaveTechBlogLikePort saveTechBlogLikePort;

    @Override
    @Transactional(readOnly = true)
    public TechBlogLikeInfo getLikeState(String typeValue, String slug, Long viewerMemberId) {
        TechBlogContentType type = TechBlogContentType.fromPath(typeValue);
        return loadTechBlogContentPort.findByTypeAndSlug(type, slug)
            .map(content -> new TechBlogLikeInfo(
                viewerMemberId != null && loadTechBlogLikePort.existsContentLike(content.getId(), viewerMemberId),
                loadTechBlogLikePort.countContentLikes(content.getId())
            ))
            .orElseGet(() -> new TechBlogLikeInfo(false, 0));
    }

    @Override
    @Transactional
    public TechBlogLikeInfo toggle(String typeValue, String slug, Long memberId) {
        TechBlogContentType type = TechBlogContentType.fromPath(typeValue);
        TechBlogContent content = loadTechBlogContentPort.findByTypeAndSlug(type, slug)
            .orElseGet(() -> saveTechBlogContentPort.save(TechBlogContent.create(type, slug)));
        boolean liked = toggleContentLike(content.getId(), memberId);
        return new TechBlogLikeInfo(liked, loadTechBlogLikePort.countContentLikes(content.getId()));
    }

    private boolean toggleContentLike(Long contentId, Long memberId) {
        if (loadTechBlogLikePort.existsContentLike(contentId, memberId)) {
            saveTechBlogLikePort.deleteContentLike(contentId, memberId);
            return false;
        }
        saveTechBlogLikePort.saveContentLike(TechBlogContentLike.create(contentId, memberId));
        return true;
    }
}
