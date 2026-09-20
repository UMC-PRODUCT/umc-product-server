package com.umc.product.blog.application.port.in.query;

import com.umc.product.blog.application.port.in.query.dto.BlogContentCursorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesCursorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesListQuery;

public interface GetBlogSeriesUseCase {

    BlogSeriesCursorInfo getPublicSeries(BlogSeriesListQuery query);

    BlogSeriesInfo getPublicSeries(String type, String slug, Long viewerMemberId);

    BlogSeriesInfo getPreview(Long seriesId, Long viewerMemberId);

    BlogContentCursorInfo getPublicSeriesContents(String type, String slug, Long cursor, int size, String sort,
                                                  Long viewerMemberId);
}
