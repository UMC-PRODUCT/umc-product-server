package com.umc.product.techblog.adapter.in.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.techblog.application.port.in.command.DeleteTechBlogCommentUseCase;
import com.umc.product.techblog.domain.TechBlogDomainException;
import com.umc.product.techblog.domain.TechBlogErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/tech-blog/comments")
@RequiredArgsConstructor
@Tag(name = "Admin | Tech Blog 댓글", description = "관리자용 테크 블로그 댓글 API")
public class AdminTechBlogCommentController {

    private final DeleteTechBlogCommentUseCase deleteTechBlogCommentUseCase;

    @DeleteMapping("/{commentId}")
    @CheckAccess(
        resourceType = ResourceType.TECH_BLOG_COMMENT,
        permission = PermissionType.MANAGE,
        message = "관리자 댓글 삭제 권한이 없습니다."
    )
    @Operation(summary = "[ADMIN-TECH-BLOG-001] 댓글 관리자 삭제", description = "중앙 총괄단 이상 권한으로 댓글을 삭제합니다.")
    public void deleteCommentByAdmin(
        @PathVariable Long commentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long adminMemberId = requireMemberId(memberPrincipal);
        deleteTechBlogCommentUseCase.deleteByAdmin(commentId, adminMemberId);
    }

    private Long requireMemberId(MemberPrincipal memberPrincipal) {
        if (memberPrincipal == null || memberPrincipal.getMemberId() == null) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_MEMBER_ID);
        }
        return memberPrincipal.getMemberId();
    }
}
