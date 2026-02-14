package com.umc.product.community.adapter.in.web;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.community.adapter.in.web.dto.response.PostDetailResponse;
import com.umc.product.community.adapter.in.web.dto.response.PostResponse;
import com.umc.product.community.adapter.in.web.dto.response.PostSearchResponse;
import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.application.port.in.post.query.GetCommentedPostsUseCase;
import com.umc.product.community.application.port.in.post.query.GetMyPostsUseCase;
import com.umc.product.community.application.port.in.post.query.GetPostDetailUseCase;
import com.umc.product.community.application.port.in.post.query.GetPostListUseCase;
import com.umc.product.community.application.port.in.post.query.GetScrappedPostsUseCase;
import com.umc.product.community.application.port.in.post.query.PostSearchQuery;
import com.umc.product.community.application.port.in.post.query.PostSearchResult;
import com.umc.product.community.application.port.in.post.query.SearchPostUseCase;
import com.umc.product.community.domain.enums.Category;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Community | 게시글 Query", description = "")
public class PostQueryController {

    private final GetPostDetailUseCase getPostDetailUseCase;
    private final GetPostListUseCase getPostListUseCase;
    private final SearchPostUseCase searchPostUseCase;
    private final GetMyPostsUseCase getMyPostsUseCase;
    private final GetCommentedPostsUseCase getCommentedPostsUseCase;
    private final GetScrappedPostsUseCase getScrappedPostsUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다. (댓글 수, 좋아요/스크랩 여부 포함)")
    public PostDetailResponse getPostDetail(
        @PathVariable Long postId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long memberId = memberPrincipal.getMemberId();
        ChallengerInfoWithStatus challenger = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);
        return PostDetailResponse.from(getPostDetailUseCase.getPostDetail(postId, challenger.challengerId()));
    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회", description = "카테고리별로 게시글 목록을 조회합니다. (최신순 정렬)")
    public PageResponse<PostResponse> getPostList(
        @RequestParam(required = false)
        @Parameter(description = "카테고리 (LIGHTNING: 번개, QUESTION: 질문, FREE: 자유). 미지정시 전체 조회")
        Category category,
        @PageableDefault(size = 20)
        @Parameter(description = "페이지네이션 (page, size)")
        Pageable pageable
    ) {
        PostSearchQuery query = new PostSearchQuery(category);
        return PageResponse.of(
            getPostListUseCase.getPostList(query, pageable),
            PostResponse::from
        );
    }

    @GetMapping("/search")
    @Operation(summary = "게시글 검색", description = "제목과 본문에서 키워드를 검색합니다. 관련도순(제목 시작 > 제목 포함 > 본문 포함)으로 정렬됩니다.")
    public PageResponse<PostSearchResponse> search(
        @RequestParam
        @Parameter(description = "검색 키워드", example = "스터디")
        String keyword,
        @PageableDefault(size = 20)
        @Parameter(description = "페이지네이션 (page, size)")
        Pageable pageable
    ) {
        Page<PostSearchResult> results = searchPostUseCase.search(keyword, pageable);
        return PageResponse.of(results, PostSearchResponse::from);
    }

    @GetMapping("/my")
    @Operation(summary = "내가 쓴 글 조회", description = "챌린저가 작성한 게시글 목록을 조회합니다. (최신순 정렬)")
    public PageResponse<PostResponse> getMyPosts(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PageableDefault(size = 20)
        @Parameter(description = "페이지네이션 (page, size)")
        Pageable pageable
    ) {
        Long memberId = memberPrincipal.getMemberId();
        ChallengerInfoWithStatus challenger = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);
        Page<PostInfo> posts = getMyPostsUseCase.getMyPosts(challenger.challengerId(), pageable);
        return PageResponse.of(posts, PostResponse::from);
    }

    @GetMapping("/commented")
    @Operation(summary = "댓글 단 글 조회", description = "챌린저가 댓글을 단 게시글 목록을 조회합니다. (최신 댓글 순)")
    public PageResponse<PostResponse> getCommentedPosts(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PageableDefault(size = 20)
        @Parameter(description = "페이지네이션 (page, size)")
        Pageable pageable
    ) {
        Long memberId = memberPrincipal.getMemberId();
        ChallengerInfoWithStatus challenger = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);
        Page<PostInfo> posts = getCommentedPostsUseCase.getCommentedPosts(challenger.challengerId(), pageable);
        return PageResponse.of(posts, PostResponse::from);
    }

    @GetMapping("/scrapped")
    @Operation(summary = "스크랩한 글 조회", description = "챌린저가 스크랩한 게시글 목록을 조회합니다. (최신 스크랩 순)")
    public PageResponse<PostResponse> getScrappedPosts(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PageableDefault(size = 20)
        @Parameter(description = "페이지네이션 (page, size)")
        Pageable pageable
    ) {
        Long memberId = memberPrincipal.getMemberId();
        ChallengerInfoWithStatus challenger = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);
        Page<PostInfo> posts = getScrappedPostsUseCase.getScrappedPosts(challenger.challengerId(), pageable);
        return PageResponse.of(posts, PostResponse::from);
    }
}
