package com.umc.product.community.application.port.in.post.query;

import com.umc.product.community.application.port.in.PostInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetPostListUseCase {
    Page<PostInfo> getPostList(PostSearchQuery query, Pageable pageable);
}
