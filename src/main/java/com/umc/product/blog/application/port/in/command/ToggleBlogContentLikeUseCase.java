package com.umc.product.blog.application.port.in.command;

import com.umc.product.blog.application.port.in.query.dto.BlogLikeInfo;

public interface ToggleBlogContentLikeUseCase {

    BlogLikeInfo toggle(String type, String slug, Long memberId);
}
