package com.umc.product.blog.application.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.blog.application.port.in.query.dto.BlogAuthorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogCommentInfo;
import com.umc.product.blog.domain.BlogComment;
import com.umc.product.blog.domain.BlogCommentDeletionType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlogCommentInfoAssembler {

    private final GetMemberUseCase getMemberUseCase;

    public BlogCommentInfo assemble(
        BlogComment comment,
        boolean likedByMe,
        int likeCount,
        List<BlogCommentInfo> replies,
        Long viewerMemberId,
        boolean viewerIsSuperAdmin
    ) {
        Map<Long, MemberInfo> members = loadMembers(List.of(comment));
        return assemble(comment, members, likedByMe, likeCount, viewerMemberId, viewerIsSuperAdmin, replies);
    }

    public List<BlogCommentInfo> assembleTree(
        List<BlogComment> topLevelComments,
        Map<Long, List<BlogComment>> repliesByParentId,
        Map<Long, Integer> likeCounts,
        Set<Long> likedCommentIds,
        Long viewerMemberId,
        boolean viewerIsSuperAdmin
    ) {
        List<BlogComment> allComments = topLevelComments.stream()
            .flatMap(comment -> {
                List<BlogComment> replies = repliesByParentId.getOrDefault(comment.getId(), List.of());
                return java.util.stream.Stream.concat(java.util.stream.Stream.of(comment), replies.stream());
            })
            .toList();

        Map<Long, MemberInfo> members = loadMembers(allComments);

        return topLevelComments.stream()
            .map(comment -> {
                List<BlogCommentInfo> replies = repliesByParentId.getOrDefault(comment.getId(), List.of()).stream()
                    .map(reply -> assemble(
                        reply,
                        members,
                        likedCommentIds.contains(reply.getId()),
                        likeCounts.getOrDefault(reply.getId(), 0),
                        viewerMemberId,
                        viewerIsSuperAdmin,
                        List.of()
                    ))
                    .toList();

                return assemble(
                    comment,
                    members,
                    likedCommentIds.contains(comment.getId()),
                    likeCounts.getOrDefault(comment.getId(), 0),
                    viewerMemberId,
                    viewerIsSuperAdmin,
                    replies
                );
            })
            .toList();
    }

    private BlogCommentInfo assemble(
        BlogComment comment,
        Map<Long, MemberInfo> members,
        boolean likedByMe,
        int likeCount,
        Long viewerMemberId,
        boolean viewerIsSuperAdmin,
        List<BlogCommentInfo> replies
    ) {
        if (comment.isDeleted()) {
            return new BlogCommentInfo(
                comment.getId(),
                null,
                comment.displayContent(),
                comment.getCreatedAt(),
                false,
                0,
                comment.getDeletionType(),
                false,
                false,
                false,
                replies
            );
        }

        return new BlogCommentInfo(
            comment.getId(),
            authorInfo(comment, members),
            comment.getContent(),
            comment.getCreatedAt(),
            likedByMe,
            likeCount,
            BlogCommentDeletionType.NONE,
            comment.canReply(),
            canEdit(comment, viewerMemberId),
            canDelete(comment, viewerMemberId, viewerIsSuperAdmin),
            replies
        );
    }

    private boolean canEdit(BlogComment comment, Long viewerMemberId) {
        return !comment.isDeleted()
            && viewerMemberId != null
            && viewerMemberId.equals(comment.getAuthorMemberId());
    }

    private boolean canDelete(
        BlogComment comment,
        Long viewerMemberId,
        boolean viewerIsSuperAdmin
    ) {
        return !comment.isDeleted()
            && viewerMemberId != null
            && (viewerMemberId.equals(comment.getAuthorMemberId()) || viewerIsSuperAdmin);
    }

    private Map<Long, MemberInfo> loadMembers(List<BlogComment> comments) {
        Set<Long> memberIds = comments.stream()
            .filter(comment -> !comment.isDeleted())
            .filter(comment -> !comment.isAnonymous())
            .map(BlogComment::getAuthorMemberId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());

        if (memberIds.isEmpty()) {
            return Map.of();
        }
        return getMemberUseCase.findAllByIds(memberIds);
    }

    private BlogAuthorInfo authorInfo(BlogComment comment, Map<Long, MemberInfo> members) {
        if (comment.isAnonymous()) {
            return guestAuthor(comment);
        }

        MemberInfo member = members.get(comment.getAuthorMemberId());
        if (member == null) {
            return null;
        }

        return new BlogAuthorInfo(
            member.id(),
            member.name(),
            member.nickname(),
            member.profileImageLink()
        );
    }

    private BlogAuthorInfo guestAuthor(BlogComment comment) {
        String nickname = comment.getNickname() != null ? comment.getNickname() : "익명";
        return new BlogAuthorInfo(null, null, nickname, null);
    }
}
