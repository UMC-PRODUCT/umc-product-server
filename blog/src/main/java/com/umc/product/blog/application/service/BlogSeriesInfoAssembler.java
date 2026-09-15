package com.umc.product.blog.application.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.blog.application.port.in.query.dto.BlogAuthorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesSummaryInfo;
import com.umc.product.blog.application.port.out.LoadBlogSeriesPort;
import com.umc.product.blog.domain.BlogSeries;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlogSeriesInfoAssembler {

    private final BlogAuthorAssembler authorAssembler;
    private final LoadBlogSeriesPort loadBlogSeriesPort;

    public BlogSeriesInfo assemble(BlogSeries series, Long viewerMemberId, boolean viewerIsSuperAdmin) {
        Map<Long, BlogAuthorInfo> authors = authorAssembler.assemble(Set.of(series.getAuthorMemberId()));
        int contentCount = loadBlogSeriesPort.countPublishedContentsBySeriesIds(List.of(series.getId()))
            .getOrDefault(series.getId(), 0);
        return toInfo(series, authors, contentCount, viewerMemberId, viewerIsSuperAdmin);
    }

    public List<BlogSeriesSummaryInfo> assembleSummaries(
        List<BlogSeries> seriesList,
        Long viewerMemberId,
        boolean viewerIsSuperAdmin
    ) {
        Set<Long> authorIds = seriesList.stream().map(BlogSeries::getAuthorMemberId).collect(Collectors.toSet());
        Map<Long, BlogAuthorInfo> authors = authorAssembler.assemble(authorIds);
        Map<Long, Integer> counts = loadBlogSeriesPort.countPublishedContentsBySeriesIds(
            seriesList.stream().map(BlogSeries::getId).toList()
        );
        return seriesList.stream()
            .map(series -> toSummary(series, authors, counts.getOrDefault(series.getId(), 0), viewerMemberId,
                viewerIsSuperAdmin))
            .toList();
    }

    private BlogSeriesInfo toInfo(
        BlogSeries series,
        Map<Long, BlogAuthorInfo> authors,
        int contentCount,
        Long viewerMemberId,
        boolean viewerIsSuperAdmin
    ) {
        return new BlogSeriesInfo(
            series.getId(),
            series.getContentType(),
            series.getSlug(),
            series.getTitle(),
            series.getDescription(),
            series.getThumbnailUrl(),
            authors.get(series.getAuthorMemberId()),
            contentCount,
            series.getUpdatedAt(),
            series.canonicalPath(),
            series.resolvedSeoTitle(),
            series.resolvedSeoDescription(),
            series.resolvedOgImageUrl(),
            canEdit(series, viewerMemberId),
            canDelete(series, viewerMemberId, viewerIsSuperAdmin)
        );
    }

    private BlogSeriesSummaryInfo toSummary(
        BlogSeries series,
        Map<Long, BlogAuthorInfo> authors,
        int contentCount,
        Long viewerMemberId,
        boolean viewerIsSuperAdmin
    ) {
        return new BlogSeriesSummaryInfo(
            series.getId(),
            series.getContentType(),
            series.getSlug(),
            series.getTitle(),
            series.getDescription(),
            series.getThumbnailUrl(),
            authors.get(series.getAuthorMemberId()),
            contentCount,
            series.getUpdatedAt(),
            series.canonicalPath(),
            series.resolvedSeoTitle(),
            series.resolvedSeoDescription(),
            series.resolvedOgImageUrl(),
            canEdit(series, viewerMemberId),
            canDelete(series, viewerMemberId, viewerIsSuperAdmin)
        );
    }

    private boolean canEdit(BlogSeries series, Long viewerMemberId) {
        return series.isAuthor(viewerMemberId) && !series.isDeleted();
    }

    private boolean canDelete(BlogSeries series, Long viewerMemberId, boolean viewerIsSuperAdmin) {
        return !series.isDeleted() && viewerMemberId != null && (series.isAuthor(viewerMemberId) || viewerIsSuperAdmin);
    }
}
