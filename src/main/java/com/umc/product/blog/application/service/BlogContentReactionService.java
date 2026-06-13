package com.umc.product.blog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.blog.application.port.in.command.ToggleBlogContentLikeUseCase;
import com.umc.product.blog.application.port.in.query.GetBlogContentLikeUseCase;
import com.umc.product.blog.application.port.in.query.dto.BlogLikeInfo;
import com.umc.product.blog.application.port.out.LoadBlogContentPort;
import com.umc.product.blog.application.port.out.LoadBlogLikePort;
import com.umc.product.blog.application.port.out.SaveBlogLikePort;
import com.umc.product.blog.domain.BlogContent;
import com.umc.product.blog.domain.BlogContentLike;
import com.umc.product.blog.domain.BlogContentType;
import com.umc.product.blog.domain.BlogDomainException;
import com.umc.product.blog.domain.BlogErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogContentReactionService implements GetBlogContentLikeUseCase, ToggleBlogContentLikeUseCase {

    private final LoadBlogContentPort loadBlogContentPort;
    private final LoadBlogLikePort loadBlogLikePort;
    private final SaveBlogLikePort saveBlogLikePort;

    @Override
    @Transactional(readOnly = true)
    public BlogLikeInfo getLikeState(String typeValue, String slug, Long viewerMemberId) {
        BlogContentType type = BlogContentType.fromPath(typeValue);
        return loadBlogContentPort.findPublishedByTypeAndSlug(type, slug)
            .map(content -> new BlogLikeInfo(
                viewerMemberId != null && loadBlogLikePort.existsContentLike(content.getId(), viewerMemberId),
                loadBlogLikePort.countContentLikes(content.getId())
            ))
            .orElseGet(() -> new BlogLikeInfo(false, 0));
    }

    @Override
    @Transactional
    public BlogLikeInfo toggle(String typeValue, String slug, Long memberId) {
        BlogContentType type = BlogContentType.fromPath(typeValue);
        BlogContent content = loadBlogContentPort.findPublishedByTypeAndSlug(type, slug)
            .orElseThrow(() -> new BlogDomainException(BlogErrorCode.CONTENT_NOT_PUBLISHED));
        boolean liked = toggleContentLike(content.getId(), memberId);
        return new BlogLikeInfo(liked, loadBlogLikePort.countContentLikes(content.getId()));
    }

    private boolean toggleContentLike(Long contentId, Long memberId) {
        if (loadBlogLikePort.existsContentLike(contentId, memberId)) {
            saveBlogLikePort.deleteContentLike(contentId, memberId);
            return false;
        }
        saveBlogLikePort.saveContentLike(BlogContentLike.create(contentId, memberId));
        return true;
    }
}
