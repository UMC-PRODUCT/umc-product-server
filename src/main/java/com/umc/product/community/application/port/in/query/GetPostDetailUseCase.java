package com.umc.product.community.application.port.in.query;

import com.umc.product.community.application.port.in.query.dto.PostDetailInfo;
import com.umc.product.community.application.port.in.query.dto.PostInfo;

public interface GetPostDetailUseCase {
    PostDetailInfo getPostDetail(Long postId, Long challengerId);  // challengerId: 좋아요 여부 확인용

    PostInfo getPostDetail(Long postId);
}
