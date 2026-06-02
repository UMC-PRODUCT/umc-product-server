package com.umc.product.techblog.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.techblog.adapter.in.web.dto.request.CreateTechBlogCommentRequest;
import com.umc.product.techblog.adapter.in.web.dto.request.UpdateTechBlogCommentRequest;
import com.umc.product.techblog.adapter.in.web.dto.response.TechBlogCommentResponse;
import com.umc.product.techblog.adapter.in.web.dto.response.TechBlogLikeResponse;
import com.umc.product.techblog.application.port.in.command.CreateTechBlogCommentUseCase;
import com.umc.product.techblog.application.port.in.command.DeleteTechBlogCommentUseCase;
import com.umc.product.techblog.application.port.in.command.ToggleTechBlogCommentLikeUseCase;
import com.umc.product.techblog.application.port.in.command.ToggleTechBlogContentLikeUseCase;
import com.umc.product.techblog.application.port.in.command.UpdateTechBlogCommentUseCase;
import com.umc.product.techblog.application.port.in.command.dto.DeleteTechBlogCommentCommand;
import com.umc.product.techblog.application.port.in.query.GetTechBlogCommentListUseCase;
import com.umc.product.techblog.application.port.in.query.GetTechBlogContentLikeUseCase;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogCommentCursorInfo;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogCommentListQuery;
import com.umc.product.techblog.domain.TechBlogDomainException;
import com.umc.product.techblog.domain.TechBlogErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tech-blog/contents/{type}/{slug}")
@RequiredArgsConstructor
@Tag(name = "Tech Blog | 댓글/좋아요", description = "정적 테크 블로그 콘텐츠 댓글 및 좋아요 API")
public class TechBlogInteractionController {

    private final GetTechBlogContentLikeUseCase getTechBlogContentLikeUseCase;
    private final ToggleTechBlogContentLikeUseCase toggleTechBlogContentLikeUseCase;
    private final GetTechBlogCommentListUseCase getTechBlogCommentListUseCase;
    private final CreateTechBlogCommentUseCase createTechBlogCommentUseCase;
    private final UpdateTechBlogCommentUseCase updateTechBlogCommentUseCase;
    private final DeleteTechBlogCommentUseCase deleteTechBlogCommentUseCase;
    private final ToggleTechBlogCommentLikeUseCase toggleTechBlogCommentLikeUseCase;

    @Public
    @GetMapping("/like")
    @Operation(summary = "[TECH-BLOG-001] 콘텐츠 좋아요 상태 조회", description = "콘텐츠 좋아요 수와 현재 사용자의 좋아요 여부를 조회합니다.")
    public TechBlogLikeResponse getContentLike(
        @PathVariable String type,
        @PathVariable String slug,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long viewerMemberId = getNullableMemberId(memberPrincipal);
        return TechBlogLikeResponse.from(
            getTechBlogContentLikeUseCase.getLikeState(type, slug, viewerMemberId)
        );
    }

    @PostMapping("/like")
    @Operation(summary = "[TECH-BLOG-002] 콘텐츠 좋아요 토글", description = "콘텐츠 좋아요를 토글합니다.")
    public TechBlogLikeResponse toggleContentLike(
        @PathVariable String type,
        @PathVariable String slug,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return TechBlogLikeResponse.from(
            toggleTechBlogContentLikeUseCase.toggle(type, slug, requireMemberId(memberPrincipal))
        );
    }

    @Public
    @GetMapping("/comments")
    @Operation(summary = "[TECH-BLOG-003] 댓글 목록 조회", description = "최상위 댓글을 커서 기반으로 조회하고 1단계 대댓글을 포함합니다.")
    public CursorResponse<TechBlogCommentResponse> getComments(
        @PathVariable String type,
        @PathVariable String slug,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        TechBlogCommentCursorInfo info = getTechBlogCommentListUseCase.getComments(
            TechBlogCommentListQuery.of(type, slug, cursor, size, sort, getNullableMemberId(memberPrincipal))
        );
        List<TechBlogCommentResponse> comments = info.content().stream()
            .map(TechBlogCommentResponse::from)
            .toList();

        return CursorResponse.of(comments, info.nextCursor(), info.hasNext());
    }

    @Public
    @PostMapping("/comments")
    @Operation(summary = "[TECH-BLOG-004] 댓글 작성", description = "콘텐츠에 댓글 또는 1단계 대댓글을 작성합니다.")
    public TechBlogCommentResponse createComment(
        @PathVariable String type,
        @PathVariable String slug,
        @Valid @RequestBody CreateTechBlogCommentRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return TechBlogCommentResponse.from(
            createTechBlogCommentUseCase.create(request.toCommand(type, slug, getNullableMemberId(memberPrincipal)))
        );
    }

    @PatchMapping("/comments/{commentId}")
    @Operation(summary = "[TECH-BLOG-005] 댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
    public TechBlogCommentResponse updateComment(
        @PathVariable String type,
        @PathVariable String slug,
        @PathVariable Long commentId,
        @Valid @RequestBody UpdateTechBlogCommentRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return TechBlogCommentResponse.from(
            updateTechBlogCommentUseCase.update(request.toCommand(type, slug, commentId, requireMemberId(memberPrincipal)))
        );
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "[TECH-BLOG-006] 댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다.")
    public void deleteComment(
        @PathVariable String type,
        @PathVariable String slug,
        @PathVariable Long commentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        deleteTechBlogCommentUseCase.delete(
            DeleteTechBlogCommentCommand.of(type, slug, commentId, requireMemberId(memberPrincipal))
        );
    }

    @PostMapping("/comments/{commentId}/like")
    @Operation(summary = "[TECH-BLOG-007] 댓글 좋아요 토글", description = "댓글 좋아요를 토글합니다.")
    public TechBlogLikeResponse toggleCommentLike(
        @PathVariable String type,
        @PathVariable String slug,
        @PathVariable Long commentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return TechBlogLikeResponse.from(
            toggleTechBlogCommentLikeUseCase.toggle(type, slug, commentId, requireMemberId(memberPrincipal))
        );
    }

    private Long getNullableMemberId(MemberPrincipal memberPrincipal) {
        return memberPrincipal == null ? null : memberPrincipal.getMemberId();
    }

    private Long requireMemberId(MemberPrincipal memberPrincipal) {
        Long memberId = getNullableMemberId(memberPrincipal);
        if (memberId == null) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_MEMBER_ID);
        }
        return memberId;
    }
}
