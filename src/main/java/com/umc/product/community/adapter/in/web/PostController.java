package com.umc.product.community.adapter.in.web;

import com.umc.product.community.adapter.in.web.dto.request.CreateLightningRequest;
import com.umc.product.community.adapter.in.web.dto.request.CreatePostRequest;
import com.umc.product.community.adapter.in.web.dto.request.UpdatePostRequest;
import com.umc.product.community.adapter.in.web.dto.response.PostResponse;
import com.umc.product.community.application.port.in.post.CreatePostUseCase;
import com.umc.product.community.application.port.in.post.DeletePostUseCase;
import com.umc.product.community.application.port.in.post.UpdatePostUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "게시글 Command API")
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;

    @PostMapping
    @Operation(summary = "일반 게시글 생성", description = "일반 게시글을 생성합니다. 번개글은 별도 API를 사용하세요.")
    public PostResponse createPost(@RequestBody CreatePostRequest request) {
        return PostResponse.from(createPostUseCase.createPost(request.toCommand()));
    }

    @PostMapping("/lightning")
    @Operation(summary = "번개글 생성", description = "번개 모임 게시글을 생성합니다.")
    public PostResponse createLightningPost(@RequestBody CreateLightningRequest request) {
        return PostResponse.from(createPostUseCase.createLightningPost(request.toCommand()));
    }

    @PatchMapping("/{postId}")
    @Operation(summary = "게시글 수정", description = "게시글의 제목과 내용을 수정합니다.")
    public PostResponse updatePost(
            @PathVariable Long postId,
            @RequestBody UpdatePostRequest request
    ) {
        return PostResponse.from(updatePostUseCase.updatePost(request.toCommand(postId)));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    public void deletePost(@PathVariable Long postId) {
        deletePostUseCase.deletePost(postId);
    }
}

