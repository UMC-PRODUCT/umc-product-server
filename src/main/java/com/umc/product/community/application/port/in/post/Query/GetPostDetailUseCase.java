package com.umc.product.community.application.port.in.post.Query;

import com.umc.product.community.application.port.in.PostInfo;

public interface GetPostDetailUseCase {
    PostInfo getPostDetail(Long postId);
}
