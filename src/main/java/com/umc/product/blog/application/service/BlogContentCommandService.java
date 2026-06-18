package com.umc.product.blog.application.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.blog.application.port.in.command.CreateBlogContentUseCase;
import com.umc.product.blog.application.port.in.command.DeleteBlogContentUseCase;
import com.umc.product.blog.application.port.in.command.UpdateBlogContentUseCase;
import com.umc.product.blog.application.port.in.command.dto.CreateBlogContentCommand;
import com.umc.product.blog.application.port.in.command.dto.DeleteBlogContentCommand;
import com.umc.product.blog.application.port.in.command.dto.UpdateBlogContentCommand;
import com.umc.product.blog.application.port.in.query.dto.BlogContentInfo;
import com.umc.product.blog.application.port.out.LoadBlogContentPort;
import com.umc.product.blog.application.port.out.LoadBlogHashtagPort;
import com.umc.product.blog.application.port.out.SaveBlogContentPort;
import com.umc.product.blog.application.port.out.SaveBlogHashtagPort;
import com.umc.product.blog.domain.BlogContent;
import com.umc.product.blog.domain.BlogContentHashtag;
import com.umc.product.blog.domain.BlogContentStatus;
import com.umc.product.blog.domain.BlogContentType;
import com.umc.product.blog.domain.BlogDomainException;
import com.umc.product.blog.domain.BlogErrorCode;
import com.umc.product.blog.domain.BlogHashtag;
import com.umc.product.global.exception.constant.Domain;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogContentCommandService implements CreateBlogContentUseCase, UpdateBlogContentUseCase,
    DeleteBlogContentUseCase {

    private static final int MAX_HASHTAG_COUNT = 10;

    private final LoadBlogContentPort loadBlogContentPort;
    private final SaveBlogContentPort saveBlogContentPort;
    private final LoadBlogHashtagPort loadBlogHashtagPort;
    private final SaveBlogHashtagPort saveBlogHashtagPort;
    private final BlogContentInfoAssembler contentInfoAssembler;

    @Audited(
        domain = Domain.BLOG,
        action = AuditAction.CREATE,
        targetType = "BlogContent",
        targetId = "#result.id()",
        description = "'블로그 콘텐츠가 생성되었습니다.'"
    )
    @Override
    public BlogContentInfo create(CreateBlogContentCommand command) {
        BlogContentType type = BlogContentType.fromPath(command.type());
        if (loadBlogContentPort.existsContentByTypeAndSlug(type, command.slug(), null)) {
            throw new BlogDomainException(BlogErrorCode.CONTENT_ALREADY_EXISTS);
        }

        BlogContent content = BlogContent.create(
            type,
            command.slug(),
            command.title(),
            command.summary(),
            command.thumbnailUrl(),
            command.content(),
            command.status() == null ? BlogContentStatus.DRAFT : command.status(),
            command.authorMemberId(),
            command.seoTitle(),
            command.seoDescription(),
            command.ogImageUrl()
        );
        BlogContent saved = saveBlogContentPort.save(content);
        replaceHashtags(saved.getId(), command.hashtags());
        return contentInfoAssembler.assemble(saved, command.authorMemberId(), true);
    }

    @Audited(
        domain = Domain.BLOG,
        action = AuditAction.UPDATE,
        targetType = "BlogContent",
        targetId = "#command.contentId()",
        description = "'블로그 콘텐츠가 수정되었습니다.'"
    )
    @Override
    public BlogContentInfo update(UpdateBlogContentCommand command) {
        BlogContent content = loadBlogContentPort.findContentById(command.contentId())
            .orElseThrow(() -> new BlogDomainException(BlogErrorCode.CONTENT_NOT_FOUND));
        if (loadBlogContentPort.existsContentByTypeAndSlug(content.getContentType(), command.slug(), content.getId())) {
            throw new BlogDomainException(BlogErrorCode.CONTENT_ALREADY_EXISTS);
        }
        content.update(
            command.slug(),
            command.title(),
            command.summary(),
            command.thumbnailUrl(),
            command.content(),
            command.status(),
            command.seoTitle(),
            command.seoDescription(),
            command.ogImageUrl()
        );
        BlogContent saved = saveBlogContentPort.save(content);
        replaceHashtags(saved.getId(), command.hashtags());
        return contentInfoAssembler.assemble(saved, content.getAuthorMemberId(), false);
    }

    @Audited(
        domain = Domain.BLOG,
        action = AuditAction.DELETE,
        targetType = "BlogContent",
        targetId = "#command.contentId()",
        description = "'블로그 콘텐츠가 삭제되었습니다.'"
    )
    @Override
    public void delete(DeleteBlogContentCommand command) {
        BlogContent content = loadBlogContentPort.findContentById(command.contentId())
            .orElseThrow(() -> new BlogDomainException(BlogErrorCode.CONTENT_NOT_FOUND));
        content.softDelete(command.memberId());
        saveBlogContentPort.save(content);
    }

    private void replaceHashtags(Long contentId, List<String> hashtagNames) {
        List<BlogHashtag> hashtags = resolveHashtags(hashtagNames);
        saveBlogHashtagPort.deleteContentHashtags(contentId);
        List<BlogContentHashtag> relations = new ArrayList<>();
        for (int i = 0; i < hashtags.size(); i++) {
            relations.add(BlogContentHashtag.create(contentId, hashtags.get(i).getId(), i));
        }
        saveBlogHashtagPort.saveContentHashtags(relations);
    }

    private List<BlogHashtag> resolveHashtags(List<String> hashtagNames) {
        if (hashtagNames == null || hashtagNames.isEmpty()) {
            return List.of();
        }
        Map<String, String> displayNameByNormalizedName = new LinkedHashMap<>();
        for (String hashtagName : hashtagNames) {
            displayNameByNormalizedName.putIfAbsent(BlogHashtag.normalize(hashtagName), hashtagName);
        }
        if (displayNameByNormalizedName.size() > MAX_HASHTAG_COUNT) {
            throw new BlogDomainException(BlogErrorCode.TOO_MANY_HASHTAGS);
        }

        List<BlogHashtag> result = new ArrayList<>();
        for (String normalizedName : displayNameByNormalizedName.keySet()) {
            BlogHashtag hashtag = loadBlogHashtagPort.findByNormalizedName(normalizedName)
                .orElseGet(() -> saveBlogHashtagPort.save(BlogHashtag.create(
                    displayNameByNormalizedName.get(normalizedName)
                )));
            result.add(hashtag);
        }
        return result;
    }
}
