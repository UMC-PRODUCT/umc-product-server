package com.umc.product.blog.adapter.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.umc.product.blog.application.port.out.LoadBlogCommentPort;
import com.umc.product.blog.application.port.out.LoadBlogContentPort;
import com.umc.product.blog.application.port.out.LoadBlogHashtagPort;
import com.umc.product.blog.application.port.out.LoadBlogLikePort;
import com.umc.product.blog.application.port.out.LoadBlogSeoPort;
import com.umc.product.blog.application.port.out.LoadBlogSeriesPort;
import com.umc.product.blog.application.port.out.SaveBlogCommentPort;
import com.umc.product.blog.application.port.out.SaveBlogContentPort;
import com.umc.product.blog.application.port.out.SaveBlogHashtagPort;
import com.umc.product.blog.application.port.out.SaveBlogLikePort;
import com.umc.product.blog.application.port.out.SaveBlogSeriesContentPort;
import com.umc.product.blog.application.port.out.SaveBlogSeriesPort;
import com.umc.product.blog.application.port.out.dto.BlogSeoPathRow;
import com.umc.product.blog.domain.BlogComment;
import com.umc.product.blog.domain.BlogCommentLike;
import com.umc.product.blog.domain.BlogCommentSort;
import com.umc.product.blog.domain.BlogContent;
import com.umc.product.blog.domain.BlogContentHashtag;
import com.umc.product.blog.domain.BlogContentLike;
import com.umc.product.blog.domain.BlogContentSort;
import com.umc.product.blog.domain.BlogContentStatus;
import com.umc.product.blog.domain.BlogContentType;
import com.umc.product.blog.domain.BlogDomainException;
import com.umc.product.blog.domain.BlogErrorCode;
import com.umc.product.blog.domain.BlogHashtag;
import com.umc.product.blog.domain.BlogHashtagSort;
import com.umc.product.blog.domain.BlogSeries;
import com.umc.product.blog.domain.BlogSeriesContent;
import com.umc.product.blog.domain.BlogSeriesSort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlogPersistenceAdapter implements LoadBlogContentPort, SaveBlogContentPort,
    LoadBlogCommentPort, SaveBlogCommentPort, LoadBlogLikePort, SaveBlogLikePort, LoadBlogSeriesPort,
    SaveBlogSeriesPort, LoadBlogHashtagPort, SaveBlogHashtagPort, SaveBlogSeriesContentPort, LoadBlogSeoPort {

    private final BlogContentJpaRepository contentJpaRepository;
    private final BlogContentLikeJpaRepository contentLikeJpaRepository;
    private final BlogCommentJpaRepository commentJpaRepository;
    private final BlogCommentLikeJpaRepository commentLikeJpaRepository;
    private final BlogCommentQueryRepository commentQueryRepository;
    private final BlogCommentLikeQueryRepository commentLikeQueryRepository;
    private final BlogContentQueryRepository contentQueryRepository;
    private final BlogSeriesJpaRepository seriesJpaRepository;
    private final BlogSeriesContentJpaRepository seriesContentJpaRepository;
    private final BlogSeriesQueryRepository seriesQueryRepository;
    private final BlogHashtagJpaRepository hashtagJpaRepository;
    private final BlogContentHashtagJpaRepository contentHashtagJpaRepository;
    private final BlogHashtagQueryRepository hashtagQueryRepository;
    private final BlogSeoQueryRepository seoQueryRepository;

    @Override
    public Optional<BlogContent> findContentById(Long contentId) {
        return contentJpaRepository.findById(contentId);
    }

    @Override
    public Optional<BlogContent> findByTypeAndSlug(BlogContentType type, String slug) {
        return contentJpaRepository.findByContentTypeAndSlug(type, slug);
    }

    @Override
    public Optional<BlogContent> findPublishedByTypeAndSlug(BlogContentType type, String slug) {
        return contentJpaRepository.findByContentTypeAndSlugAndStatus(type, slug, BlogContentStatus.PUBLISHED)
            .filter(content -> content.getDeletedAt() == null);
    }

    @Override
    public List<BlogContent> listPublicContents(
        BlogContentType type,
        String seriesSlug,
        String hashtagSlug,
        BlogContentSort sort,
        Long cursor,
        int limit
    ) {
        return contentQueryRepository.listPublicContents(type, seriesSlug, hashtagSlug, sort, cursor, limit);
    }

    @Override
    public List<BlogContent> listPublicSeriesContents(Long seriesId, Long cursor, int limit) {
        return contentQueryRepository.listPublicSeriesContents(seriesId, cursor, limit);
    }

    @Override
    public List<BlogContent> listPublicHashtagContents(
        Long hashtagId,
        BlogContentType type,
        BlogContentSort sort,
        Long cursor,
        int limit
    ) {
        return contentQueryRepository.listPublicHashtagContents(hashtagId, type, sort, cursor, limit);
    }

    @Override
    public boolean existsContentByTypeAndSlug(BlogContentType type, String slug, Long excludedContentId) {
        if (excludedContentId == null) {
            return contentJpaRepository.existsByContentTypeAndSlug(type, slug);
        }
        return contentJpaRepository.existsByContentTypeAndSlugAndIdNot(type, slug, excludedContentId);
    }

    @Override
    public List<BlogContent> listByIds(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return List.of();
        }
        return contentJpaRepository.findAllById(contentIds);
    }

    @Override
    public int countContentLikes(Long contentId) {
        return contentLikeJpaRepository.countByContentId(contentId);
    }

    @Override
    public boolean existsContentLike(Long contentId, Long memberId) {
        return memberId != null && contentLikeJpaRepository.existsByContentIdAndMemberId(contentId, memberId);
    }

    @Override
    public BlogContent save(BlogContent content) {
        return contentJpaRepository.save(content);
    }

    @Override
    public BlogContentLike saveContentLike(BlogContentLike like) {
        return contentLikeJpaRepository.save(like);
    }

    @Override
    public void deleteContentLike(Long contentId, Long memberId) {
        contentLikeJpaRepository.deleteByContentIdAndMemberId(contentId, memberId);
    }

    @Override
    public Optional<BlogComment> findById(Long commentId) {
        return commentJpaRepository.findById(commentId);
    }

    @Override
    public BlogComment getByIdAndContentId(Long commentId, Long contentId) {
        return commentJpaRepository.findByIdAndContentId(commentId, contentId)
            .orElseThrow(() -> new BlogDomainException(BlogErrorCode.COMMENT_NOT_FOUND));
    }

    @Override
    public List<BlogComment> listTopLevel(Long contentId, BlogCommentSort sort, Long cursor, int size) {
        return commentQueryRepository.listTopLevel(contentId, sort, cursor, size);
    }

    @Override
    public List<BlogComment> listRepliesByParentIds(List<Long> parentCommentIds) {
        return commentQueryRepository.listRepliesByParentIds(parentCommentIds);
    }

    @Override
    public boolean existsVisibleReply(Long parentCommentId) {
        return commentQueryRepository.existsVisibleReply(parentCommentId);
    }

    @Override
    public int countCommentLikes(Long commentId) {
        return commentLikeJpaRepository.countByCommentId(commentId);
    }

    @Override
    public Map<Long, Integer> countCommentLikesByCommentIds(List<Long> commentIds) {
        return commentLikeQueryRepository.countByCommentIds(commentIds);
    }

    @Override
    public Set<Long> findLikedCommentIds(List<Long> commentIds, Long memberId) {
        return commentLikeQueryRepository.findLikedCommentIds(commentIds, memberId);
    }

    @Override
    public BlogComment save(BlogComment comment) {
        return commentJpaRepository.save(comment);
    }

    @Override
    public void hardDelete(Long commentId) {
        commentJpaRepository.deleteById(commentId);
    }

    @Override
    public boolean existsCommentLike(Long commentId, Long memberId) {
        return memberId != null && commentLikeJpaRepository.existsByCommentIdAndMemberId(commentId, memberId);
    }

    @Override
    public BlogCommentLike saveCommentLike(BlogCommentLike like) {
        return commentLikeJpaRepository.save(like);
    }

    @Override
    public void deleteCommentLike(Long commentId, Long memberId) {
        commentLikeJpaRepository.deleteByCommentIdAndMemberId(commentId, memberId);
    }

    @Override
    public void deleteCommentLikesByCommentId(Long commentId) {
        commentLikeJpaRepository.deleteByCommentId(commentId);
    }

    @Override
    public Optional<BlogSeries> findSeriesById(Long seriesId) {
        return seriesJpaRepository.findById(seriesId);
    }

    @Override
    public Optional<BlogSeries> findSeriesByTypeAndSlug(BlogContentType type, String slug) {
        return seriesJpaRepository.findByContentTypeAndSlug(type, slug);
    }

    @Override
    public List<BlogSeries> listPublicSeries(BlogContentType type, BlogSeriesSort sort, Long cursor, int limit) {
        return seriesQueryRepository.listPublicSeries(type, sort, cursor, limit);
    }

    @Override
    public List<BlogSeries> listSeriesByContentIds(List<Long> contentIds) {
        return seriesQueryRepository.listByContentIds(contentIds);
    }

    @Override
    public Map<Long, Integer> countPublishedContentsBySeriesIds(List<Long> seriesIds) {
        return seriesQueryRepository.countPublishedContentsBySeriesIds(seriesIds);
    }

    @Override
    public Map<Long, List<BlogSeries>> listSeriesByContentIdsGrouped(List<Long> contentIds) {
        return seriesQueryRepository.listByContentIdsGrouped(contentIds);
    }

    @Override
    public boolean existsSeriesByTypeAndSlug(BlogContentType type, String slug, Long excludedSeriesId) {
        if (excludedSeriesId == null) {
            return seriesJpaRepository.existsByContentTypeAndSlug(type, slug);
        }
        return seriesJpaRepository.existsByContentTypeAndSlugAndIdNot(type, slug, excludedSeriesId);
    }

    @Override
    public boolean hasPublishedContent(Long seriesId) {
        return seriesQueryRepository.hasPublishedContent(seriesId);
    }

    @Override
    public BlogSeries save(BlogSeries series) {
        return seriesJpaRepository.save(series);
    }

    @Override
    public Optional<BlogHashtag> findBySlug(String slug) {
        return hashtagJpaRepository.findBySlug(slug);
    }

    @Override
    public Optional<BlogHashtag> findByNormalizedName(String normalizedName) {
        return hashtagJpaRepository.findByNormalizedName(normalizedName);
    }

    @Override
    public List<BlogHashtag> listPublicHashtags(
        BlogContentType type,
        String q,
        BlogHashtagSort sort,
        Long cursor,
        int limit
    ) {
        return hashtagQueryRepository.listPublicHashtags(type, q, sort, cursor, limit);
    }

    @Override
    public List<BlogHashtag> listHashtagsByContentIds(List<Long> contentIds) {
        return hashtagQueryRepository.listByContentIds(contentIds);
    }

    @Override
    public Map<Long, List<BlogHashtag>> listHashtagsByContentIdsGrouped(List<Long> contentIds) {
        return hashtagQueryRepository.listByContentIdsGrouped(contentIds);
    }

    @Override
    public Map<Long, Integer> countPublishedContentsByHashtagIds(List<Long> hashtagIds) {
        return hashtagQueryRepository.countPublishedContentsByHashtagIds(hashtagIds);
    }

    @Override
    public Map<Long, Integer> countPublishedContentsByHashtagIds(List<Long> hashtagIds, BlogContentType type) {
        return hashtagQueryRepository.countPublishedContentsByHashtagIds(hashtagIds, type);
    }

    @Override
    public BlogHashtag save(BlogHashtag hashtag) {
        return hashtagJpaRepository.save(hashtag);
    }

    @Override
    public void deleteContentHashtags(Long contentId) {
        contentHashtagJpaRepository.deleteByContentId(contentId);
    }

    @Override
    public List<BlogContentHashtag> saveContentHashtags(List<BlogContentHashtag> contentHashtags) {
        return contentHashtagJpaRepository.saveAll(contentHashtags);
    }

    @Override
    public void deleteBySeriesId(Long seriesId) {
        seriesContentJpaRepository.deleteBySeriesId(seriesId);
    }

    @Override
    public List<BlogSeriesContent> saveAll(List<BlogSeriesContent> seriesContents) {
        return seriesContentJpaRepository.saveAll(seriesContents);
    }

    @Override
    public List<BlogSeoPathRow> listPublicSeoPaths() {
        return seoQueryRepository.listPublicSeoPaths();
    }
}
