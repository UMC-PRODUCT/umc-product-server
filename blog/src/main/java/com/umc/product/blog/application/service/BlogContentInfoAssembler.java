package com.umc.product.blog.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.umc.product.blog.application.port.in.query.dto.BlogContentInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogContentSummaryInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogHashtagInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesSummaryInfo;
import com.umc.product.blog.application.port.out.LoadBlogHashtagPort;
import com.umc.product.blog.application.port.out.LoadBlogSeriesPort;
import com.umc.product.blog.domain.BlogContent;
import com.umc.product.blog.domain.BlogHashtag;
import com.umc.product.blog.domain.BlogSeries;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlogContentInfoAssembler {

    private final BlogAuthorAssembler authorAssembler;
    private final LoadBlogSeriesPort loadBlogSeriesPort;
    private final LoadBlogHashtagPort loadBlogHashtagPort;

    public BlogContentInfo assemble(BlogContent content, Long viewerMemberId, boolean viewerIsSuperAdmin) {
        return assemble(List.of(content), viewerMemberId, viewerIsSuperAdmin).getFirst();
    }

    public List<BlogContentInfo> assemble(List<BlogContent> contents, Long viewerMemberId, boolean viewerIsSuperAdmin) {
        BlogContentRelations relations = loadRelations(contents);
        return contents.stream()
            .map(content -> toInfo(content, relations, viewerMemberId, viewerIsSuperAdmin))
            .toList();
    }

    public List<BlogContentSummaryInfo> assembleSummaries(
        List<BlogContent> contents,
        Long viewerMemberId,
        boolean viewerIsSuperAdmin
    ) {
        BlogContentRelations relations = loadRelations(contents);
        return contents.stream()
            .map(content -> toSummary(content, relations, viewerMemberId, viewerIsSuperAdmin))
            .toList();
    }

    private BlogContentInfo toInfo(
        BlogContent content,
        BlogContentRelations relations,
        Long viewerMemberId,
        boolean viewerIsSuperAdmin
    ) {
        return new BlogContentInfo(
            content.getId(),
            content.getContentType(),
            content.getSlug(),
            content.getTitle(),
            content.getSummary(),
            content.getThumbnailUrl(),
            content.getContent(),
            content.getStatus(),
            relations.authors().get(content.getAuthorMemberId()),
            content.getPublishedAt(),
            content.getUpdatedAt(),
            content.canonicalPath(),
            content.resolvedSeoTitle(),
            content.resolvedSeoDescription(),
            content.resolvedOgImageUrl(),
            seriesSummaries(content, relations, viewerMemberId, viewerIsSuperAdmin),
            hashtagInfos(content, relations),
            canEdit(content, viewerMemberId),
            canDelete(content, viewerMemberId, viewerIsSuperAdmin)
        );
    }

    private BlogContentSummaryInfo toSummary(
        BlogContent content,
        BlogContentRelations relations,
        Long viewerMemberId,
        boolean viewerIsSuperAdmin
    ) {
        return new BlogContentSummaryInfo(
            content.getId(),
            content.getContentType(),
            content.getSlug(),
            content.getTitle(),
            content.getSummary(),
            content.getThumbnailUrl(),
            content.getStatus(),
            relations.authors().get(content.getAuthorMemberId()),
            content.getPublishedAt(),
            content.getUpdatedAt(),
            content.canonicalPath(),
            content.resolvedSeoTitle(),
            content.resolvedSeoDescription(),
            content.resolvedOgImageUrl(),
            seriesSummaries(content, relations, viewerMemberId, viewerIsSuperAdmin),
            hashtagInfos(content, relations),
            canEdit(content, viewerMemberId),
            canDelete(content, viewerMemberId, viewerIsSuperAdmin)
        );
    }

    private List<BlogSeriesSummaryInfo> seriesSummaries(
        BlogContent content,
        BlogContentRelations relations,
        Long viewerMemberId,
        boolean viewerIsSuperAdmin
    ) {
        return relations.seriesByContentId().getOrDefault(content.getId(), List.of()).stream()
            .map(series -> new BlogSeriesSummaryInfo(
                series.getId(),
                series.getContentType(),
                series.getSlug(),
                series.getTitle(),
                series.getDescription(),
                series.getThumbnailUrl(),
                relations.authors().get(series.getAuthorMemberId()),
                relations.seriesContentCounts().getOrDefault(series.getId(), 0),
                series.getUpdatedAt(),
                series.canonicalPath(),
                series.resolvedSeoTitle(),
                series.resolvedSeoDescription(),
                series.resolvedOgImageUrl(),
                canEdit(series, viewerMemberId),
                canDelete(series, viewerMemberId, viewerIsSuperAdmin)
            ))
            .toList();
    }

    private List<BlogHashtagInfo> hashtagInfos(BlogContent content, BlogContentRelations relations) {
        return relations.hashtagsByContentId().getOrDefault(content.getId(), List.of()).stream()
            .map(hashtag -> new BlogHashtagInfo(
                hashtag.getId(),
                hashtag.getName(),
                hashtag.getSlug(),
                relations.hashtagContentCounts().getOrDefault(hashtag.getId(), 0)
            ))
            .toList();
    }

    private BlogContentRelations loadRelations(List<BlogContent> contents) {
        if (contents == null || contents.isEmpty()) {
            return BlogContentRelations.empty();
        }
        List<Long> contentIds = contents.stream().map(BlogContent::getId).toList();
        Map<Long, List<BlogSeries>> seriesByContentId = loadBlogSeriesPort.listSeriesByContentIdsGrouped(contentIds);
        Map<Long, List<BlogHashtag>> hashtagsByContentId = loadBlogHashtagPort.listHashtagsByContentIdsGrouped(contentIds);
        List<Long> seriesIds = seriesByContentId.values().stream()
            .flatMap(List::stream)
            .map(BlogSeries::getId)
            .distinct()
            .toList();
        List<Long> hashtagIds = hashtagsByContentId.values().stream()
            .flatMap(List::stream)
            .map(BlogHashtag::getId)
            .distinct()
            .toList();

        Set<Long> authorMemberIds = new HashSet<>();
        contents.forEach(content -> authorMemberIds.add(content.getAuthorMemberId()));
        seriesByContentId.values().stream()
            .flatMap(List::stream)
            .map(BlogSeries::getAuthorMemberId)
            .forEach(authorMemberIds::add);

        return new BlogContentRelations(
            seriesByContentId,
            hashtagsByContentId,
            loadBlogSeriesPort.countPublishedContentsBySeriesIds(seriesIds),
            loadBlogHashtagPort.countPublishedContentsByHashtagIds(hashtagIds),
            authorAssembler.assemble(authorMemberIds)
        );
    }

    private boolean canEdit(BlogContent content, Long viewerMemberId) {
        return content.isAuthor(viewerMemberId) && !content.isDeleted();
    }

    private boolean canDelete(BlogContent content, Long viewerMemberId, boolean viewerIsSuperAdmin) {
        return !content.isDeleted() && viewerMemberId != null && (content.isAuthor(viewerMemberId) || viewerIsSuperAdmin);
    }

    private boolean canEdit(BlogSeries series, Long viewerMemberId) {
        return series.isAuthor(viewerMemberId) && !series.isDeleted();
    }

    private boolean canDelete(BlogSeries series, Long viewerMemberId, boolean viewerIsSuperAdmin) {
        return !series.isDeleted() && viewerMemberId != null && (series.isAuthor(viewerMemberId) || viewerIsSuperAdmin);
    }

    private record BlogContentRelations(
        Map<Long, List<BlogSeries>> seriesByContentId,
        Map<Long, List<BlogHashtag>> hashtagsByContentId,
        Map<Long, Integer> seriesContentCounts,
        Map<Long, Integer> hashtagContentCounts,
        Map<Long, com.umc.product.blog.application.port.in.query.dto.BlogAuthorInfo> authors
    ) {
        static BlogContentRelations empty() {
            return new BlogContentRelations(Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }
    }
}
