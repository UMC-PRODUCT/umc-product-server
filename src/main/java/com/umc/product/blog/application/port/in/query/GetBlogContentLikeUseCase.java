package com.umc.product.blog.application.port.in.query;

import com.umc.product.blog.application.port.in.query.dto.BlogLikeInfo;

public interface GetBlogContentLikeUseCase {

    BlogLikeInfo getLikeState(String type, String slug, Long viewerMemberId);
}
