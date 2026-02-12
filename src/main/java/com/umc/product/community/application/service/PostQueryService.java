package com.umc.product.community.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.application.port.in.post.query.GetCommentedPostsUseCase;
import com.umc.product.community.application.port.in.post.query.GetMyPostsUseCase;
import com.umc.product.community.application.port.in.post.query.GetPostDetailUseCase;
import com.umc.product.community.application.port.in.post.query.GetPostListUseCase;
import com.umc.product.community.application.port.in.post.query.GetScrappedPostsUseCase;
import com.umc.product.community.application.port.in.post.query.PostDetailInfo;
import com.umc.product.community.application.port.in.post.query.PostSearchQuery;
import com.umc.product.community.application.port.in.post.query.PostSearchResult;
import com.umc.product.community.application.port.in.post.query.SearchPostUseCase;
import com.umc.product.community.application.port.out.LoadCommentPort;
import com.umc.product.community.application.port.out.LoadPostPort;
import com.umc.product.community.application.port.out.LoadScrapPort;
import com.umc.product.community.application.port.out.PostSearchData;
import com.umc.product.community.application.port.out.PostWithAuthor;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService implements GetPostDetailUseCase, GetPostListUseCase, SearchPostUseCase,
        GetMyPostsUseCase, GetCommentedPostsUseCase, GetScrappedPostsUseCase {

    private final LoadPostPort loadPostPort;
    private final LoadCommentPort loadCommentPort;
    private final LoadScrapPort loadScrapPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final AuthorInfoProvider authorInfoProvider;

    @Override
    public PostInfo getPostDetail(Long postId) {
        PostWithAuthor postWithAuthor = loadPostPort.findByIdWithAuthor(postId)
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        String authorName = authorInfoProvider.getAuthorName(postWithAuthor.authorChallengerId());
        return PostInfo.from(postWithAuthor.post(), postWithAuthor.authorChallengerId(), authorName);
    }

    @Override
    public PostDetailInfo getPostDetail(Long postId, Long challengerId) {
        PostWithAuthor postWithAuthor = loadPostPort.findByIdWithAuthor(postId, challengerId)
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        String authorName = authorInfoProvider.getAuthorName(postWithAuthor.authorChallengerId());
        PostInfo postInfo = PostInfo.from(postWithAuthor.post(), postWithAuthor.authorChallengerId(), authorName);
        int commentCount = loadCommentPort.countByPostId(postId);

        // 작성자 파트 정보 조회
        ChallengerInfo authorChallengerInfo = getChallengerUseCase.getChallengerPublicInfo(postWithAuthor.authorChallengerId());

        // 스크랩 정보 조회
        boolean isScrapped = loadScrapPort.existsByPostIdAndChallengerId(postId, challengerId);
        int scrapCount = loadScrapPort.countByPostId(postId);

        return PostDetailInfo.of(postInfo, commentCount, authorChallengerInfo.part(), isScrapped, scrapCount);
    }

    @Override
    public Page<PostInfo> getPostList(PostSearchQuery query, Pageable pageable) {
        Page<Post> posts = loadPostPort.findAllByQuery(query, pageable);

        // 게시글이 없으면 빈 페이지 반환
        if (posts.isEmpty()) {
            return posts.map(post -> PostInfo.from(post, null, null));
        }

        // 1. 게시글 ID 목록 추출
        List<Long> postIds = posts.stream()
                .map(post -> post.getPostId().id())
                .toList();

        // 2. 게시글 ID -> 작성자 챌린저 ID 매핑 (1 query)
        Map<Long, Long> postIdToAuthorId = loadPostPort.findAuthorIdsByPostIds(postIds);

        // 3. 고유한 챌린저 ID 목록 추출
        Set<Long> challengerIds = Set.copyOf(postIdToAuthorId.values());

        // 4. 챌린저 ID -> 챌린저 정보 매핑 (1 query)
        Map<Long, ChallengerInfo> challengerInfoMap = getChallengerUseCase.getChallengerPublicInfoByIds(challengerIds);

        // 5. 멤버 ID 목록 추출
        Set<Long> memberIds = challengerInfoMap.values().stream()
                .map(ChallengerInfo::memberId)
                .collect(Collectors.toSet());

        // 6. 멤버 ID -> 멤버 프로필 매핑 (1 query, 일괄 조회로 N+1 해결)
        Map<Long, MemberProfileInfo> memberProfileMap = getMemberUseCase.getProfiles(memberIds);

        // 7. 챌린저 ID -> 작성자 이름 매핑
        Map<Long, String> authorNameMap = challengerInfoMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Long memberId = entry.getValue().memberId();
                            MemberProfileInfo memberProfile = memberProfileMap.get(memberId);
                            return memberProfile != null ? memberProfile.name() : "알 수 없음";
                        }
                ));

        // 8. PostInfo로 변환
        return posts.map(post -> {
            Long postId = post.getPostId().id();
            Long authorId = postIdToAuthorId.get(postId);
            String authorName = authorId != null ? authorNameMap.get(authorId) : "알 수 없음";
            return PostInfo.from(post, authorId, authorName);
        });
    }

    @Override
    public Page<PostSearchResult> search(String keyword, Pageable pageable) {
        Page<PostSearchData> searchDataPage = loadPostPort.searchByKeyword(keyword, pageable);

        return searchDataPage.map(PostSearchData::toResult);
    }

    @Override
    public Page<PostInfo> getMyPosts(Long challengerId, Pageable pageable) {
        Page<Post> posts = loadPostPort.findByAuthorChallengerId(challengerId, pageable);

        // 게시글이 없으면 빈 페이지 반환
        if (posts.isEmpty()) {
            return Page.empty(pageable);
        }

        // 작성자 이름 조회 (한 번만)
        String authorName = authorInfoProvider.getAuthorName(challengerId);

        // PostInfo로 변환
        return posts.map(post -> PostInfo.from(post, challengerId, authorName));
    }

    @Override
    public Page<PostInfo> getCommentedPosts(Long challengerId, Pageable pageable) {
        Page<Post> posts = loadPostPort.findCommentedPostsByChallengerId(challengerId, pageable);

        return convertToPostInfoPage(posts, pageable);
    }

    @Override
    public Page<PostInfo> getScrappedPosts(Long challengerId, Pageable pageable) {
        Page<Post> posts = loadPostPort.findScrappedPostsByChallengerId(challengerId, pageable);

        return convertToPostInfoPage(posts, pageable);
    }

    /**
     * Post 페이지를 PostInfo 페이지로 변환 (작성자 정보 포함)
     */
    private Page<PostInfo> convertToPostInfoPage(Page<Post> posts, Pageable pageable) {
        // 게시글이 없으면 빈 페이지 반환
        if (posts.isEmpty()) {
            return Page.empty(pageable);
        }

        // 1. 게시글 ID 목록 추출
        List<Long> postIds = posts.stream()
                .map(post -> post.getPostId().id())
                .toList();

        // 2. 게시글 ID -> 작성자 챌린저 ID 매핑 (1 query)
        Map<Long, Long> postIdToAuthorId = loadPostPort.findAuthorIdsByPostIds(postIds);

        // 3. 고유한 챌린저 ID 목록 추출
        Set<Long> challengerIds = Set.copyOf(postIdToAuthorId.values());

        // 4. 챌린저 ID -> 챌린저 정보 매핑 (1 query)
        Map<Long, ChallengerInfo> challengerInfoMap = getChallengerUseCase.getChallengerPublicInfoByIds(challengerIds);

        // 5. 멤버 ID 목록 추출
        Set<Long> memberIds = challengerInfoMap.values().stream()
                .map(ChallengerInfo::memberId)
                .collect(Collectors.toSet());

        // 6. 멤버 ID -> 멤버 프로필 매핑 (1 query, 일괄 조회로 N+1 해결)
        Map<Long, MemberProfileInfo> memberProfileMap = getMemberUseCase.getProfiles(memberIds);

        // 7. 챌린저 ID -> 작성자 이름 매핑
        Map<Long, String> authorNameMap = challengerInfoMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Long memberId = entry.getValue().memberId();
                            MemberProfileInfo memberProfile = memberProfileMap.get(memberId);
                            return memberProfile != null ? memberProfile.name() : "알 수 없음";
                        }
                ));

        // 8. PostInfo로 변환
        return posts.map(post -> {
            Long postId = post.getPostId().id();
            Long authorId = postIdToAuthorId.get(postId);
            String authorName = authorId != null ? authorNameMap.get(authorId) : "알 수 없음";
            return PostInfo.from(post, authorId, authorName);
        });
    }
}
