package com.umc.product.techblog.adapter.in.web;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.techblog.application.port.in.command.DeleteTechBlogCommentUseCase;
import com.umc.product.techblog.domain.TechBlogDomainException;
import com.umc.product.techblog.domain.TechBlogErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/tech-blog/comments")
@RequiredArgsConstructor
@Tag(name = "Admin | Tech Blog 댓글", description = "관리자용 테크 블로그 댓글 API")
public class AdminTechBlogCommentController {

    private final DeleteTechBlogCommentUseCase deleteTechBlogCommentUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @DeleteMapping("/{commentId}")
    @Operation(summary = "[ADMIN-TECH-BLOG-001] 댓글 관리자 삭제", description = "중앙 총괄단 이상 권한으로 댓글을 삭제합니다.")
    public void deleteCommentByAdmin(
        @PathVariable Long commentId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long adminMemberId = requireMemberId(memberPrincipal);
        validateAdmin(adminMemberId);
        deleteTechBlogCommentUseCase.deleteByAdmin(commentId, adminMemberId);
    }

    private void validateAdmin(Long memberId) {
        boolean allowed = getChallengerRoleUseCase.findAllByMemberId(memberId).stream()
            .map(ChallengerRoleInfo::roleType)
            .anyMatch(roleType -> roleType != null && roleType.isAtLeastCentralCore());

        if (!allowed) {
            throw new TechBlogDomainException(TechBlogErrorCode.ADMIN_DELETE_FORBIDDEN);
        }
    }

    private Long requireMemberId(MemberPrincipal memberPrincipal) {
        if (memberPrincipal == null || memberPrincipal.getMemberId() == null) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_MEMBER_ID);
        }
        return memberPrincipal.getMemberId();
    }
}
