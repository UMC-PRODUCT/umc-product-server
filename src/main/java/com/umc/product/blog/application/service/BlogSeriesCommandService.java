package com.umc.product.blog.application.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.blog.application.port.in.command.CreateBlogSeriesUseCase;
import com.umc.product.blog.application.port.in.command.DeleteBlogSeriesUseCase;
import com.umc.product.blog.application.port.in.command.ReplaceBlogSeriesContentsUseCase;
import com.umc.product.blog.application.port.in.command.UpdateBlogSeriesUseCase;
import com.umc.product.blog.application.port.in.command.dto.CreateBlogSeriesCommand;
import com.umc.product.blog.application.port.in.command.dto.DeleteBlogSeriesCommand;
import com.umc.product.blog.application.port.in.command.dto.ReplaceBlogSeriesContentsCommand;
import com.umc.product.blog.application.port.in.command.dto.UpdateBlogSeriesCommand;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesInfo;
import com.umc.product.blog.application.port.out.LoadBlogContentPort;
import com.umc.product.blog.application.port.out.LoadBlogSeriesPort;
import com.umc.product.blog.application.port.out.SaveBlogSeriesContentPort;
import com.umc.product.blog.application.port.out.SaveBlogSeriesPort;
import com.umc.product.blog.domain.BlogContent;
import com.umc.product.blog.domain.BlogContentType;
import com.umc.product.blog.domain.BlogDomainException;
import com.umc.product.blog.domain.BlogErrorCode;
import com.umc.product.blog.domain.BlogSeries;
import com.umc.product.blog.domain.BlogSeriesContent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogSeriesCommandService implements CreateBlogSeriesUseCase, UpdateBlogSeriesUseCase,
    DeleteBlogSeriesUseCase, ReplaceBlogSeriesContentsUseCase {

    private final LoadBlogSeriesPort loadBlogSeriesPort;
    private final SaveBlogSeriesPort saveBlogSeriesPort;
    private final LoadBlogContentPort loadBlogContentPort;
    private final SaveBlogSeriesContentPort saveBlogSeriesContentPort;
    private final BlogSeriesInfoAssembler seriesInfoAssembler;

    @Override
    public BlogSeriesInfo create(CreateBlogSeriesCommand command) {
        BlogContentType type = BlogContentType.fromPath(command.type());
        if (loadBlogSeriesPort.existsSeriesByTypeAndSlug(type, command.slug(), null)) {
            throw new BlogDomainException(BlogErrorCode.SERIES_ALREADY_EXISTS);
        }
        BlogSeries series = BlogSeries.create(
            type,
            command.slug(),
            command.title(),
            command.description(),
            command.thumbnailUrl(),
            command.authorMemberId(),
            command.seoTitle(),
            command.seoDescription(),
            command.ogImageUrl()
        );
        BlogSeries saved = saveBlogSeriesPort.save(series);
        return seriesInfoAssembler.assemble(saved, command.authorMemberId(), true);
    }

    @Override
    public BlogSeriesInfo update(UpdateBlogSeriesCommand command) {
        BlogSeries series = getSeries(command.seriesId());
        if (loadBlogSeriesPort.existsSeriesByTypeAndSlug(series.getContentType(), command.slug(), series.getId())) {
            throw new BlogDomainException(BlogErrorCode.SERIES_ALREADY_EXISTS);
        }
        series.update(
            command.slug(),
            command.title(),
            command.description(),
            command.thumbnailUrl(),
            command.seoTitle(),
            command.seoDescription(),
            command.ogImageUrl()
        );
        return seriesInfoAssembler.assemble(saveBlogSeriesPort.save(series), series.getAuthorMemberId(), false);
    }

    @Override
    public void delete(DeleteBlogSeriesCommand command) {
        BlogSeries series = getSeries(command.seriesId());
        series.softDelete(command.memberId());
        saveBlogSeriesPort.save(series);
    }

    @Override
    public BlogSeriesInfo replaceContents(ReplaceBlogSeriesContentsCommand command) {
        BlogSeries series = getSeries(command.seriesId());
        List<Long> contentIds = new ArrayList<>(new LinkedHashSet<>(command.contentIds()));
        List<BlogContent> contents = loadBlogContentPort.listByIds(contentIds);
        if (contents.size() != contentIds.size()) {
            throw new BlogDomainException(BlogErrorCode.CONTENT_NOT_FOUND);
        }
        for (BlogContent content : contents) {
            if (content.isDeleted()) {
                throw new BlogDomainException(BlogErrorCode.CONTENT_NOT_FOUND);
            }
            if (content.getContentType() != series.getContentType()) {
                throw new BlogDomainException(BlogErrorCode.CONTENT_TYPE_MISMATCH);
            }
        }

        saveBlogSeriesContentPort.deleteBySeriesId(series.getId());
        List<BlogSeriesContent> relations = new ArrayList<>();
        for (int i = 0; i < contentIds.size(); i++) {
            relations.add(BlogSeriesContent.create(series.getId(), contentIds.get(i), i));
        }
        saveBlogSeriesContentPort.saveAll(relations);
        return seriesInfoAssembler.assemble(series, series.getAuthorMemberId(), false);
    }

    private BlogSeries getSeries(Long seriesId) {
        return loadBlogSeriesPort.findSeriesById(seriesId)
            .filter(series -> !series.isDeleted())
            .orElseThrow(() -> new BlogDomainException(BlogErrorCode.SERIES_NOT_FOUND));
    }
}
