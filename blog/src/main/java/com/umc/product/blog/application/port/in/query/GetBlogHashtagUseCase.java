package com.umc.product.blog.application.port.in.query;

import com.umc.product.blog.application.port.in.query.dto.BlogContentCursorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogHashtagCursorInfo;

public interface GetBlogHashtagUseCase {

    BlogHashtagCursorInfo getPublicHashtags(String type, String q, Long cursor, int size, String sort);

    BlogContentCursorInfo getPublicHashtagContents(String hashtagSlug, String type, Long cursor, int size, String sort,
                                                   Long viewerMemberId);
}
