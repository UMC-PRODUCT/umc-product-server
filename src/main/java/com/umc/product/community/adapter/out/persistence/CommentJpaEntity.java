package com.umc.product.community.adapter.out.persistence;

import com.umc.product.common.BaseEntity;
import com.umc.product.community.domain.Comment;
import com.umc.product.community.domain.Comment.CommentId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
        return Comment.reconstruct(
                new CommentId(id),
                postId,
                challengerId,
                content,
                parentId
        );
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
