package com.umc.product.community.adapter.in.web;

import com.umc.product.community.adapter.in.web.dto.response.PostResponse;
import com.umc.product.community.application.port.in.post.Query.GetPostDetailUseCase;
import com.umc.product.community.application.port.in.post.Query.GetPostListUseCase;
import com.umc.product.community.application.port.in.post.Query.PostSearchQuery;
import com.umc.product.community.domain.enums.PostSortType;
import com.umc.product.global.constant.SwaggerTag.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다.")
    public PostResponse getPostDetail(@PathVariable Long postId) {
        return PostResponse.from(getPostDetailUseCase.getPostDetail(postId));
    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회", description = "모집중 여부, 정렬 기준으로 게시글 목록을 조회합니다.")
    public List<PostResponse> getPostList(
            @RequestParam(defaultValue = "false") boolean ing,
            @RequestParam(defaultValue = "ALL") PostSortType sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PostSearchQuery query = new PostSearchQuery(ing, sort, page, size);
        return getPostListUseCase.getPostList(query)
                .stream()
                .map(PostResponse::from)
                .toList();
    }
}
