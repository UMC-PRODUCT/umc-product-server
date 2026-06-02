package com.umc.product.techblog.application.service.evaluator;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.techblog.application.port.out.LoadTechBlogCommentPort;
import com.umc.product.techblog.domain.TechBlogComment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class TechBlogCommentPermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadTechBlogCommentPort loadTechBlogCommentPort;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.TECH_BLOG_COMMENT;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        PermissionType permission = resourcePermission.permission();
        Long commentId = resourcePermission.getResourceIdAsLong();
        if (commentId == null) {
            return false;
        }

        TechBlogComment comment = loadTechBlogCommentPort.findById(commentId).orElse(null);
        if (comment == null) {
            return false;
        }

        return switch (permission) {
            case EDIT -> isAuthor(subjectAttributes.memberId(), comment);
            case DELETE -> isAuthor(subjectAttributes.memberId(), comment) || isSuperAdmin(subjectAttributes);
            default -> {
                log.warn("TechBlogCommentPermissionEvaluator에서 지원하지 않는 PermissionType: {}", permission);
                yield false;
            }
        };
    }

    private boolean isAuthor(Long memberId, TechBlogComment comment) {
        return memberId != null
            && comment.getAuthorMemberId() != null
            && comment.getAuthorMemberId().equals(memberId);
    }

    private boolean isSuperAdmin(SubjectAttributes subjectAttributes) {
        return subjectAttributes.roleAttributes().stream()
            .map(RoleAttribute::roleType)
            .anyMatch(roleType -> roleType != null && roleType.isSuperAdmin());
    }
}
