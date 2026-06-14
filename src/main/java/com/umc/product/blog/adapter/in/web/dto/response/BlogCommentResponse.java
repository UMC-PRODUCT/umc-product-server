package com.umc.product.blog.adapter.in.web.dto.response;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.umc.product.blog.application.port.in.query.dto.BlogAuthorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogCommentInfo;
import com.umc.product.blog.domain.BlogCommentDeletionType;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "블로그 댓글 응답")
public record BlogCommentResponse(
    @Schema(description = "댓글 ID", example = "1")
    Long id,

    @Schema(description = "작성자 정보. 삭제 placeholder에서는 생략됩니다.")
    AuthorResponse author,

    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다.")
    String content,

    @Schema(description = "작성일시", example = "2026-06-03T10:30:00Z")
    Instant createdAt,

    @Schema(description = "현재 사용자의 댓글 좋아요 여부", example = "false")
    boolean likedByMe,

    @Schema(description = "댓글 좋아요 수", example = "3")
    int likeCount,

    @Schema(description = "삭제 상태", example = "NONE")
    BlogCommentDeletionType deletionType,

    @Schema(description = "대댓글 작성 가능 여부", example = "true")
    boolean canReply,

    @Schema(description = "내가 수정할 수 있는지 여부", example = "true")
    boolean canEdit,

    @Schema(description = "내가 삭제할 수 있는지 여부", example = "true")
    boolean canDelete,

    @Schema(description = "1단계 대댓글 목록")
    List<BlogCommentResponse> replies
) {

    public static BlogCommentResponse from(BlogCommentInfo info) {
        return new BlogCommentResponse(
            info.id(),
            AuthorResponse.from(info.author()),
            info.content(),
            info.createdAt(),
            info.likedByMe(),
            info.likeCount(),
            info.deletionType(),
            info.canReply(),
            info.canEdit(),
            info.canDelete(),
            info.replies().stream().map(BlogCommentResponse::from).toList()
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "블로그 댓글 작성자 응답")
    public record AuthorResponse(
        @Schema(description = "회원 ID. 비회원 작성자면 null입니다.", example = "1")
        Long id,

        @Schema(description = "회원 이름", example = "홍길동")
        String name,

        @Schema(description = "닉네임", example = "spring-master")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png")
        String profileImageUrl
    ) {

        public static AuthorResponse from(BlogAuthorInfo info) {
            if (info == null) {
                return null;
            }
            return new AuthorResponse(info.id(), info.name(), info.nickname(), info.profileImageUrl());
        }
    }
}
