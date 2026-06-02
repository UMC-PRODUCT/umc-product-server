package com.umc.product.techblog.application.service;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogAuthorInfo;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogCommentInfo;
import com.umc.product.techblog.domain.TechBlogComment;
import com.umc.product.techblog.domain.TechBlogCommentDeletionType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechBlogCommentInfoAssembler {

    private final GetMemberUseCase getMemberUseCase;

    public TechBlogCommentInfo assemble(
        TechBlogComment comment,
        boolean likedByMe,
        int likeCount,
        List<TechBlogCommentInfo> replies
    ) {
        Map<Long, MemberInfo> members = loadMembers(List.of(comment));
        return assemble(comment, members, likedByMe, likeCount, replies);
    }

    public List<TechBlogCommentInfo> assembleTree(
        List<TechBlogComment> topLevelComments,
        Map<Long, List<TechBlogComment>> repliesByParentId,
        Map<Long, Integer> likeCounts,
        Set<Long> likedCommentIds
    ) {
        List<TechBlogComment> allComments = topLevelComments.stream()
            .flatMap(comment -> {
                List<TechBlogComment> replies = repliesByParentId.getOrDefault(comment.getId(), List.of());
                return java.util.stream.Stream.concat(java.util.stream.Stream.of(comment), replies.stream());
            })
            .toList();

        Map<Long, MemberInfo> members = loadMembers(allComments);

        return topLevelComments.stream()
            .map(comment -> {
                List<TechBlogCommentInfo> replies = repliesByParentId.getOrDefault(comment.getId(), List.of()).stream()
                    .map(reply -> assemble(
                        reply,
                        members,
                        likedCommentIds.contains(reply.getId()),
                        likeCounts.getOrDefault(reply.getId(), 0),
                        List.of()
                    ))
                    .toList();

                return assemble(
                    comment,
                    members,
                    likedCommentIds.contains(comment.getId()),
                    likeCounts.getOrDefault(comment.getId(), 0),
                    replies
                );
            })
            .toList();
    }

    private TechBlogCommentInfo assemble(
        TechBlogComment comment,
        Map<Long, MemberInfo> members,
        boolean likedByMe,
        int likeCount,
        List<TechBlogCommentInfo> replies
    ) {
        if (comment.isDeleted()) {
            return new TechBlogCommentInfo(
                comment.getId(),
                null,
                comment.displayContent(),
                comment.getCreatedAt(),
                false,
                0,
                comment.getDeletionType(),
                false,
                replies
            );
        }

        return new TechBlogCommentInfo(
            comment.getId(),
            authorInfo(comment, members),
            comment.getContent(),
            comment.getCreatedAt(),
            likedByMe,
            likeCount,
            TechBlogCommentDeletionType.NONE,
            comment.canReply(),
            replies
        );
    }

    private Map<Long, MemberInfo> loadMembers(List<TechBlogComment> comments) {
        Set<Long> memberIds = comments.stream()
            .filter(comment -> !comment.isDeleted())
            .filter(comment -> !comment.isAnonymous())
            .map(TechBlogComment::getAuthorMemberId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());

        if (memberIds.isEmpty()) {
            return Map.of();
        }
        return getMemberUseCase.findAllByIds(memberIds);
    }

    private TechBlogAuthorInfo authorInfo(TechBlogComment comment, Map<Long, MemberInfo> members) {
        if (comment.isAnonymous()) {
            return guestAuthor(comment);
        }

        MemberInfo member = members.get(comment.getAuthorMemberId());
        if (member == null) {
            return null;
        }

        return new TechBlogAuthorInfo(
            member.id(),
            member.name(),
            member.nickname(),
            member.profileImageLink()
        );
    }

    private TechBlogAuthorInfo guestAuthor(TechBlogComment comment) {
        String nickname = comment.getGuestNickname() != null ? comment.getGuestNickname() : "익명";
        return new TechBlogAuthorInfo(null, null, nickname, null);
    }
}
