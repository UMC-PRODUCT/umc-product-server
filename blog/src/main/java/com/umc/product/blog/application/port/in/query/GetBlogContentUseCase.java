package com.umc.product.blog.application.port.in.query;

import com.umc.product.blog.application.port.in.query.dto.BlogContentCursorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogContentInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogContentListQuery;
import com.umc.product.blog.application.port.in.query.dto.BlogSeoPathsInfo;

public interface GetBlogContentUseCase {

    BlogContentCursorInfo getPublicContents(BlogContentListQuery query);

    BlogContentInfo getPublicContent(String type, String slug, Long viewerMemberId);

    BlogContentInfo getPreview(Long contentId, Long viewerMemberId);

    BlogSeoPathsInfo getSeoPaths();
}
