package com.umc.product.community.adapter.in.web;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.community.adapter.in.web.dto.request.CreateCommentRequest;
import com.umc.product.community.adapter.in.web.dto.response.CommentResponse;
import com.umc.product.community.adapter.in.web.dto.response.LikeResponse;
import com.umc.product.community.application.port.in.command.comment.CreateCommentUseCase;
import com.umc.product.community.application.port.in.command.comment.DeleteCommentUseCase;
import com.umc.product.community.application.port.in.command.comment.ToggleCommentLikeUseCase;
import com.umc.product.community.application.port.in.query.GetCommentListUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "Community | 댓글", description = "댓글 관련 API")
public class CommentController {

    private final CreateCommentUseCase createCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final GetCommentListUseCase getCommentListUseCase;
    private final ToggleCommentLikeUseCase toggleCommentLikeUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    @PostMapping
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    public CommentResponse createComment(
        @PathVariable Long postId,
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody CreateCommentRequest request
    ) {
        Long memberId = memberPrincipal.getMemberId();
        ChallengerInfoWithStatus challenger = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);
        return CommentResponse.from(
            createCommentUseCase.create(request.toCommand(postId, challenger.challengerId()))
        );
    }

    @GetMapping
    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 조회합니다.")
    public List<CommentResponse> getComments(@PathVariable Long postId) {
        return getCommentListUseCase.getComments(postId).stream()
            .map(CommentResponse::from)
            .toList();
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다.")
    public void deleteComment(
        @PathVariable Long postId,
        @PathVariable Long commentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long memberId = memberPrincipal.getMemberId();
        ChallengerInfoWithStatus challenger = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);
        deleteCommentUseCase.delete(commentId, challenger.challengerId());
    }

    @PostMapping("/{commentId}/like")
    @Operation(summary = "댓글 좋아요 토글", description = "댓글 좋아요를 토글합니다. 이미 좋아요한 경우 취소됩니다.")
    public LikeResponse toggleCommentLike(
        @PathVariable Long postId,
        @PathVariable Long commentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long memberId = memberPrincipal.getMemberId();
        ChallengerInfoWithStatus challenger = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);
        return LikeResponse.from(toggleCommentLikeUseCase.toggle(commentId, challenger.challengerId()));
    }
}
