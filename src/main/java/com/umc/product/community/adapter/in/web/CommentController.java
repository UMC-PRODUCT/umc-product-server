package com.umc.product.community.adapter.in.web;

import com.umc.product.community.adapter.in.web.dto.request.CreateCommentRequest;
import com.umc.product.community.adapter.in.web.dto.response.CommentResponse;
import com.umc.product.community.application.port.in.post.CreateCommentUseCase;
import com.umc.product.community.application.port.in.post.DeleteCommentUseCase;
import com.umc.product.community.application.port.in.post.Query.GetCommentListUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 API")
public class CommentController {

    private final CreateCommentUseCase createCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final GetCommentListUseCase getCommentListUseCase;

    @PostMapping
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    public CommentResponse createComment(
            @PathVariable Long postId,
            @RequestParam Long challengerId,  // TODO: @CurrentUser로 변경 필요
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return CommentResponse.from(
                createCommentUseCase.create(request.toCommand(postId, challengerId))
        );
    }

    @GetMapping
    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 조회합니다.")
    public Page<CommentResponse> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return getCommentListUseCase.getComments(postId, pageable)
                .map(CommentResponse::from);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다.")
    public void deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long challengerId  // TODO: @CurrentUser로 변경 필요
    ) {
        deleteCommentUseCase.delete(commentId, challengerId);
    }
}
