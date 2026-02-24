package com.umc.product.community.adapter.out.persistence.entity;

import com.umc.product.common.BaseEntity;
import com.umc.product.community.domain.Comment;
import com.umc.product.community.domain.Comment.CommentId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "challenger_id", nullable = false)
    private Long challengerId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "parent_id")
    private Long parentId;

    @ElementCollection
    @CollectionTable(name = "comment_like", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "challenger_id")
    private Set<Long> likedChallengerIds = new HashSet<>();

    private CommentJpaEntity(Long postId, Long challengerId, String content, Long parentId) {
        this.postId = postId;
        this.challengerId = challengerId;
        this.content = content;
        this.parentId = parentId;
    }

    public static CommentJpaEntity from(Comment comment) {
        return new CommentJpaEntity(
            comment.getPostId(),
            comment.getChallengerId(),
            comment.getContent(),
            null
        );
    }


    public Comment toDomain() {
        return toDomain(null);
    }

    public Comment toDomain(Long viewerChallengerId) {
        boolean liked = viewerChallengerId != null && isLikedBy(viewerChallengerId);

        return Comment.reconstruct(
            new CommentId(id),
            postId,
            challengerId,
            content,
            parentId,
            getLikeCount(),
            liked,
            getCreatedAt()
        );
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public boolean toggleLike(Long challengerId) {
        if (!likedChallengerIds.remove(challengerId)) {
            likedChallengerIds.add(challengerId);
            return true;
        }
        return false;
    }

    public int getLikeCount() {
        return likedChallengerIds.size();
    }

    public boolean isLikedBy(Long challengerId) {
        return likedChallengerIds.contains(challengerId);
    }
}
