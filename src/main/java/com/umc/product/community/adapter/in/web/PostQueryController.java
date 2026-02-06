package com.umc.product.community.adapter.in.web;

import com.umc.product.community.adapter.in.web.dto.response.PostResponse;
import com.umc.product.community.adapter.in.web.dto.response.PostSearchResponse;
import com.umc.product.community.application.port.in.post.query.GetPostDetailUseCase;
import com.umc.product.community.application.port.in.post.query.GetPostListUseCase;
import com.umc.product.community.application.port.in.post.query.PostSearchQuery;
import com.umc.product.community.application.port.in.post.query.PostSearchResult;
import com.umc.product.community.application.port.in.post.query.SearchPostUseCase;
import com.umc.product.community.domain.enums.Category;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
@Tag(name = Constants.COMMUNITY)
public class PostQueryController {

    private final GetPostDetailUseCase getPostDetailUseCase;
    private final GetPostListUseCase getPostListUseCase;
    private final SearchPostUseCase searchPostUseCase;

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다.")
    public PostResponse getPostDetail(@PathVariable Long postId) {
        return PostResponse.from(getPostDetailUseCase.getPostDetail(postId));
    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회", description = "카테고리별로 게시글 목록을 조회합니다. (최신순 정렬)")
    public ApiResponse<PageResponse<PostResponse>> getPostList(
            @RequestParam(required = false)
            @Parameter(description = "카테고리 (LIGHTNING: 번개, QUESTION: 질문, FREE: 자유). 미지정시 전체 조회")
            Category category,
            @PageableDefault(size = 20)
            @Parameter(description = "페이지네이션 (page, size)")
            Pageable pageable
    ) {
        PostSearchQuery query = new PostSearchQuery(category);
        PageResponse<PostResponse> response = PageResponse.of(
                getPostListUseCase.getPostList(query, pageable),
                PostResponse::from
        );

        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/search")
    @Operation(summary = "게시글 검색", description = "제목과 본문에서 키워드를 검색합니다. 관련도순(제목 시작 > 제목 포함 > 본문 포함)으로 정렬됩니다.")
    public ApiResponse<PageResponse<PostSearchResponse>> search(
            @RequestParam
            @Parameter(description = "검색 키워드", example = "스터디")
            String keyword,
            @PageableDefault(size = 20)
            @Parameter(description = "페이지네이션 (page, size)")
            Pageable pageable
    ) {
        Page<PostSearchResult> results = searchPostUseCase.search(keyword, pageable);
        PageResponse<PostSearchResponse> response = PageResponse.of(results, PostSearchResponse::from);

        return ApiResponse.onSuccess(response);
    }
}
