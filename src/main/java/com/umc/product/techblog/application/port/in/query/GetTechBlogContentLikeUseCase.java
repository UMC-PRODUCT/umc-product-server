package com.umc.product.techblog.application.port.in.query;

import com.umc.product.techblog.application.port.in.query.dto.TechBlogLikeInfo;

public interface GetTechBlogContentLikeUseCase {

    TechBlogLikeInfo getLikeState(String type, String slug, Long viewerMemberId);
}
