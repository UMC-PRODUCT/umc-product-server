package com.umc.product.techblog.application.port.in.command;

import com.umc.product.techblog.application.port.in.query.dto.TechBlogLikeInfo;

public interface ToggleTechBlogContentLikeUseCase {

    TechBlogLikeInfo toggle(String type, String slug, Long memberId);
}
