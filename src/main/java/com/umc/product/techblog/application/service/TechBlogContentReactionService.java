package com.umc.product.techblog.application.service;

import com.umc.product.techblog.application.port.in.command.ToggleTechBlogContentLikeUseCase;
import com.umc.product.techblog.application.port.in.query.GetTechBlogContentLikeUseCase;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogLikeInfo;
import com.umc.product.techblog.application.port.out.LoadTechBlogContentPort;
import com.umc.product.techblog.application.port.out.SaveTechBlogContentPort;
import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TechBlogContentReactionService implements GetTechBlogContentLikeUseCase, ToggleTechBlogContentLikeUseCase {

    private final LoadTechBlogContentPort loadTechBlogContentPort;
    private final SaveTechBlogContentPort saveTechBlogContentPort;

    @Override
    @Transactional(readOnly = true)
    public TechBlogLikeInfo getLikeState(String typeValue, String slug, Long viewerMemberId) {
        TechBlogContentType type = TechBlogContentType.fromPath(typeValue);
        return loadTechBlogContentPort.findByTypeAndSlug(type, slug)
            .map(content -> new TechBlogLikeInfo(
                viewerMemberId != null && loadTechBlogContentPort.existsLikeByContentIdAndMemberId(
                    content.getId(), viewerMemberId),
                loadTechBlogContentPort.countLikesByContentId(content.getId())
            ))
            .orElseGet(() -> new TechBlogLikeInfo(false, 0));
    }

    @Override
    @Transactional
    public TechBlogLikeInfo toggle(String typeValue, String slug, Long memberId) {
        TechBlogContentType type = TechBlogContentType.fromPath(typeValue);
        TechBlogContent content = loadTechBlogContentPort.findByTypeAndSlug(type, slug)
            .orElseGet(() -> saveTechBlogContentPort.save(TechBlogContent.create(type, slug)));
        return saveTechBlogContentPort.toggleContentLike(content.getId(), memberId);
    }
}
