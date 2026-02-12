package com.umc.product.community.adapter.in.web;

import com.umc.product.community.adapter.in.web.dto.request.CreateLightningRequest;
import com.umc.product.community.adapter.in.web.dto.request.CreatePostRequest;
import com.umc.product.community.adapter.in.web.dto.request.UpdateLightningRequest;
import com.umc.product.community.adapter.in.web.dto.request.UpdatePostRequest;
import com.umc.product.community.adapter.in.web.dto.response.LikeResponse;
import com.umc.product.community.adapter.in.web.dto.response.PostResponse;
import com.umc.product.community.adapter.in.web.dto.response.ScrapResponse;
import com.umc.product.community.application.port.in.post.CreatePostUseCase;
import com.umc.product.community.application.port.in.post.DeletePostUseCase;
import com.umc.product.community.application.port.in.post.TogglePostLikeUseCase;
import com.umc.product.community.application.port.in.post.ToggleScrapUseCase;
import com.umc.product.community.application.port.in.post.UpdateLightningUseCase;
import com.umc.product.community.application.port.in.post.UpdatePostUseCase;
import com.umc.product.global.constant.SwaggerTag.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = Constants.COMMUNITY)
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final UpdateLightningUseCase updateLightningUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final TogglePostLikeUseCase togglePostLikeUseCase;
    private final ToggleScrapUseCase toggleScrapUseCase;

    @PostMapping
    @Operation(summary = "일반 게시글 생성", description = "일반 게시글을 생성합니다. 번개글은 별도 API를 사용하세요.")
    public PostResponse createPost(
            @RequestBody CreatePostRequest request,
            @RequestParam Long challengerId  // TODO: @CurrentUser로 변경 필요
    ) {
        return PostResponse.from(createPostUseCase.createPost(request.toCommand(challengerId)));
    }

    @PostMapping("/lightning")
    @Operation(summary = "번개글 생성", description = "번개 모임 게시글을 생성합니다.")
    public PostResponse createLightningPost(
            @RequestBody CreateLightningRequest request,
            @RequestParam Long challengerId  // TODO: @CurrentUser로 변경 필요
    ) {
        return PostResponse.from(createPostUseCase.createLightningPost(request.toCommand(challengerId)));
    }

    @PatchMapping("/{postId}")
    @Operation(summary = "일반 게시글 수정", description = "일반 게시글의 제목, 내용, 카테고리를 수정합니다.")
    public PostResponse updatePost(
            @PathVariable Long postId,
            @RequestBody UpdatePostRequest request
    ) {
        return PostResponse.from(updatePostUseCase.updatePost(request.toCommand(postId)));
    }

    @PatchMapping("/{postId}/lightning")
    @Operation(summary = "번개글 수정", description = "번개 게시글의 제목, 내용, 모임 정보를 수정합니다.")
    public PostResponse updateLightningPost(
            @PathVariable Long postId,
            @RequestBody UpdateLightningRequest request
    ) {
        return PostResponse.from(updateLightningUseCase.updateLightning(request.toCommand(postId)));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    public void deletePost(@PathVariable Long postId) {
        deletePostUseCase.deletePost(postId);
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "게시글 좋아요 토글", description = "게시글 좋아요를 토글합니다. 이미 좋아요한 경우 취소됩니다.")
    public LikeResponse toggleLike(
            @PathVariable Long postId,
            @RequestParam Long challengerId  // TODO: @CurrentUser로 변경 필요
    ) {
        return LikeResponse.from(togglePostLikeUseCase.toggleLike(postId, challengerId));
    }

    @PostMapping("/{postId}/scrap")
    @Operation(summary = "게시글 스크랩 토글", description = "게시글 스크랩을 토글합니다. 이미 스크랩한 경우 취소됩니다.")
    public ScrapResponse toggleScrap(
            @PathVariable Long postId,
            @RequestParam Long challengerId  // TODO: @CurrentUser로 변경 필요
    ) {
        return ScrapResponse.from(toggleScrapUseCase.toggleScrap(postId, challengerId));
    }
}

