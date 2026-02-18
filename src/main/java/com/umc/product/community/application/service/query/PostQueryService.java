package com.umc.product.community.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.application.port.in.command.post.dto.PostInfo;
import com.umc.product.community.application.port.in.query.GetCommentedPostsUseCase;
import com.umc.product.community.application.port.in.query.GetMyPostsUseCase;
import com.umc.product.community.application.port.in.query.GetPostDetailUseCase;
import com.umc.product.community.application.port.in.query.GetPostListUseCase;
import com.umc.product.community.application.port.in.query.GetScrappedPostsUseCase;
import com.umc.product.community.application.port.in.query.SearchPostUseCase;
import com.umc.product.community.application.port.in.query.dto.PostDetailInfo;
import com.umc.product.community.application.port.in.query.dto.PostSearchQuery;
import com.umc.product.community.application.port.in.query.dto.PostSearchResult;
import com.umc.product.community.application.port.out.comment.LoadCommentPort;
import com.umc.product.community.application.port.out.dto.PostSearchData;
import com.umc.product.community.application.port.out.dto.PostWithAuthor;
import com.umc.product.community.application.port.out.post.LoadPostPort;
import com.umc.product.community.application.port.out.scrap.LoadScrapPort;
import com.umc.product.community.application.service.AuthorInfoProvider;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
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

        Long authorChallengerId = postWithAuthor.authorChallengerId();

        // 챌린저 정보 조회
        ChallengerInfo challengerInfo = getChallengerUseCase.getChallengerPublicInfo(authorChallengerId);

        // 멤버 프로필 조회 (이름과 프로필 이미지)
        MemberInfo memberProfile = getMemberUseCase.getProfile(challengerInfo.memberId());
        String authorName = memberProfile.name();
        String authorProfileImage = memberProfile.profileImageLink();

        return PostInfo.from(postWithAuthor.post(), authorChallengerId, authorName, authorProfileImage,
            challengerInfo.part(), 0);
    }

    @Override
    public PostDetailInfo getPostDetail(Long postId, Long challengerId) {
        PostWithAuthor postWithAuthor = loadPostPort.findByIdWithAuthor(postId, challengerId)
            .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        Long authorChallengerId = postWithAuthor.authorChallengerId();

        // 작성자 챌린저 정보 조회
        ChallengerInfo authorChallengerInfo = getChallengerUseCase.getChallengerPublicInfo(authorChallengerId);

        // 작성자 멤버 프로필 조회 (이름과 프로필 이미지)
        MemberInfo memberProfile = getMemberUseCase.getProfile(authorChallengerInfo.memberId());
        String authorName = memberProfile.name();
        String authorProfileImage = memberProfile.profileImageLink();

        // 본인 작성 글 여부 확인
        boolean isAuthor = authorChallengerId.equals(challengerId);

        int commentCount = loadCommentPort.countByPostId(postId);
        PostInfo postInfo = PostInfo.from(postWithAuthor.post(), authorChallengerId, authorName, authorProfileImage,
            authorChallengerInfo.part(), commentCount, isAuthor);

        // 스크랩 정보 조회
        boolean isScrapped = loadScrapPort.existsByPostIdAndChallengerId(postId, challengerId);
        int scrapCount = loadScrapPort.countByPostId(postId);

        return PostDetailInfo.of(postInfo, commentCount, authorChallengerInfo.part(), isScrapped, scrapCount);
    }

    @Override
    public Page<PostInfo> getPostList(PostSearchQuery query, Pageable pageable) {
        Page<Post> posts = loadPostPort.findAllByQuery(query, pageable);
        return convertToPostInfoPage(posts, null, pageable);
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

        // 작성자 챌린저 정보 조회 (한 번만)
        ChallengerInfo challengerInfo = getChallengerUseCase.getChallengerPublicInfo(challengerId);

        // 작성자 멤버 프로필 조회 (이름과 프로필 이미지, 한 번만)
        MemberInfo memberProfile = getMemberUseCase.getProfile(challengerInfo.memberId());
        String authorName = memberProfile.name();
        String authorProfileImage = memberProfile.profileImageLink();

        // PostInfo로 변환 (모든 게시글이 본인 글이므로 isAuthor = true)
        return posts.map(
            post -> PostInfo.from(post, challengerId, authorName, authorProfileImage, challengerInfo.part(), 0, true));
    }

    @Override
    public Page<PostInfo> getCommentedPosts(Long challengerId, Pageable pageable) {
        Page<Post> posts = loadPostPort.findCommentedPostsByChallengerId(challengerId, pageable);

        return convertToPostInfoPage(posts, challengerId, pageable);
    }

    @Override
    public Page<PostInfo> getScrappedPosts(Long challengerId, Pageable pageable) {
        Page<Post> posts = loadPostPort.findScrappedPostsByChallengerId(challengerId, pageable);

        return convertToPostInfoPage(posts, challengerId, pageable);
    }

    /**
     * Post 페이지를 PostInfo 페이지로 변환 (작성자 정보 포함)
     *
     * @param posts               게시글 페이지
     * @param currentChallengerId 현재 로그인한 사용자의 challengerId (비로그인 시 null)
     * @param pageable            페이지 정보
     */
    private Page<PostInfo> convertToPostInfoPage(Page<Post> posts, Long currentChallengerId, Pageable pageable) {
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
        Map<Long, MemberInfo> memberProfileMap = getMemberUseCase.getProfiles(memberIds);

        // 7. 챌린저 ID -> 작성자 정보 매핑 (이름 + 프로필 이미지 + 파트, 한 번의 스트림 처리)
        Map<Long, AuthorDetails> authorDetailsMap = challengerInfoMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    Long memberId = entry.getValue().memberId();
                    MemberInfo memberProfile = memberProfileMap.get(memberId);
                    String name = memberProfile != null ? memberProfile.name() : "알 수 없음";
                    String profileImage = memberProfile != null ? memberProfile.profileImageLink() : null;
                    return new AuthorDetails(name, profileImage, entry.getValue().part());
                }
            ));

        // 8. 댓글 수 일괄 조회 (1 query)
        Map<Long, Integer> commentCountMap = loadCommentPort.countByPostIds(postIds);

        // 9. PostInfo로 변환
        return posts.map(post -> {
            Long postId = post.getPostId().id();
            Long authorId = postIdToAuthorId.get(postId);
            AuthorDetails authorDetails = authorId != null ? authorDetailsMap.get(authorId) : null;
            String authorName = authorDetails != null ? authorDetails.name() : "알 수 없음";
            String authorProfileImage = authorDetails != null ? authorDetails.profileImage() : null;
            var authorPart = authorDetails != null ? authorDetails.part() : null;
            int commentCount = commentCountMap.getOrDefault(postId, 0);
            // 본인 작성 글 여부 확인
            boolean isAuthor = currentChallengerId != null && authorId != null && authorId.equals(currentChallengerId);
            return PostInfo.from(post, authorId, authorName, authorProfileImage, authorPart, commentCount, isAuthor);
        });
    }

    /**
     * 작성자 정보를 담는 내부 record
     */
    private record AuthorDetails(String name, String profileImage, ChallengerPart part) {
    }
}
