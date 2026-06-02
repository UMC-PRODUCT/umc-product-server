package com.umc.product.techblog.application.port.in.query;

import com.umc.product.techblog.application.port.in.query.dto.TechBlogCommentCursorInfo;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogCommentListQuery;

public interface GetTechBlogCommentListUseCase {

    TechBlogCommentCursorInfo getComments(TechBlogCommentListQuery query);
}
