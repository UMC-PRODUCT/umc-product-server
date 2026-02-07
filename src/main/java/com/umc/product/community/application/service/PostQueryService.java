package com.umc.product.community.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
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
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
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
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public PostInfo getPostDetail(Long postId) {
        Post post = loadPostPort.findById(postId)
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        Long authorId = loadPostPort.findAuthorIdByPostId(postId);
        String authorName = getAuthorName(authorId);
        return PostInfo.from(post, authorId, authorName);
    }

    @Override
    public PostDetailInfo getPostDetail(Long postId, Long challengerId) {
        Post post = loadPostPort.findById(postId)
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        Long authorId = loadPostPort.findAuthorIdByPostId(postId);
        String authorName = getAuthorName(authorId);
        PostInfo postInfo = PostInfo.from(post, authorId, authorName);
        int commentCount = loadCommentPort.countByPostId(postId);

        return PostDetailInfo.of(postInfo, commentCount);
    }

    @Override
    public Page<PostInfo> getPostList(PostSearchQuery query, Pageable pageable) {
        Page<Post> posts = loadPostPort.findAllByQuery(query, pageable);

        // N+1 문제 있음 - 추후 최적화 필요
        return posts.map(post -> {
            Long authorId = loadPostPort.findAuthorIdByPostId(post.getPostId().id());
            String authorName = getAuthorName(authorId);
            return PostInfo.from(post, authorId, authorName);
        });
    }

    @Override
    public Page<PostSearchResult> search(String keyword, Pageable pageable) {
        Page<PostSearchData> searchDataPage = loadPostPort.searchByKeyword(keyword, pageable);

        return searchDataPage.map(PostSearchData::toResult);
    }

    private String getAuthorName(Long challengerId) {
        ChallengerInfo challengerInfo = getChallengerUseCase.getChallengerPublicInfo(challengerId);
        MemberProfileInfo profileInfo = getMemberUseCase.getProfile(challengerInfo.memberId());
        return profileInfo.name();
    }
}
