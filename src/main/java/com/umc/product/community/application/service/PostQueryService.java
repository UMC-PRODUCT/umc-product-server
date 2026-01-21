package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.application.port.in.post.query.GetPostDetailUseCase;
import com.umc.product.community.application.port.in.post.query.GetPostListUseCase;
import com.umc.product.community.application.port.in.post.query.PostDetailInfo;
import com.umc.product.community.application.port.in.post.query.PostSearchQuery;
import com.umc.product.community.application.port.out.LoadPostPort;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService implements GetPostDetailUseCase, GetPostListUseCase {

    private final LoadPostPort loadPostPort;

    @Override
    public PostInfo getPostDetail(Long postId) {
        // TODO: 구현 필요
        return null;
    }

    @Override
    public PostDetailInfo getPostDetail(Long postId, Long challengerId) {
        // TODO: 구현 필요
        return null;
    }

    @Override
    public List<PostInfo> getPostList(PostSearchQuery query) {
        // TODO: 구현 필요
        return Collections.emptyList();
    }
}
