package com.umc.product.community.application.port.in.query;

import com.umc.product.community.application.port.in.command.post.dto.PostInfo;
import com.umc.product.community.application.port.in.query.dto.PostSearchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetPostListUseCase {
    Page<PostInfo> getPostList(PostSearchQuery query, Pageable pageable);
}
