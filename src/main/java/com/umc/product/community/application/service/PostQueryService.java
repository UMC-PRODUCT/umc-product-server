package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.application.port.in.post.query.GetPostDetailUseCase;
import com.umc.product.community.application.port.in.post.query.GetPostListUseCase;
import com.umc.product.community.application.port.in.post.query.PostDetailInfo;
import com.umc.product.community.application.port.in.post.query.PostSearchQuery;
import com.umc.product.community.application.port.in.post.query.PostSearchResult;
import com.umc.product.community.application.port.in.post.query.SearchPostUseCase;
import com.umc.product.community.application.port.out.LoadCommentPort;
import com.umc.product.community.application.port.out.LoadPostPort;
import com.umc.product.community.application.port.out.PostSearchData;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService implements GetPostDetailUseCase, GetPostListUseCase, SearchPostUseCase {

    private final LoadPostPort loadPostPort;
    private final LoadCommentPort loadCommentPort;

    @Override
    public PostInfo getPostDetail(Long postId) {
        Post post = loadPostPort.findById(postId)
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        return PostInfo.from(post);
    }

    @Override
    public PostDetailInfo getPostDetail(Long postId, Long challengerId) {
        Post post = loadPostPort.findById(postId)
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        PostInfo postInfo = PostInfo.from(post);
        int commentCount = loadCommentPort.countByPostId(postId);

        return PostDetailInfo.of(postInfo, commentCount);
    }

    @Override
    public List<PostInfo> getPostList(PostSearchQuery query) {
        List<Post> posts = loadPostPort.findAllByQuery(query);

        return posts.stream()
                .map(PostInfo::from)
                .toList();
    }

    @Override
    public Page<PostSearchResult> search(String keyword, Pageable pageable) {
        Page<PostSearchData> searchDataPage = loadPostPort.searchByKeyword(keyword, pageable);

        return searchDataPage.map(PostSearchData::toResult);
    }
}
