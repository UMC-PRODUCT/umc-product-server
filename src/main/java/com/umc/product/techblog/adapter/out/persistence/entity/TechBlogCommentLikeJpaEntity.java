package com.umc.product.techblog.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tech_blog_comment_like")
@IdClass(TechBlogCommentLikeId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechBlogCommentLikeJpaEntity {

    @Id
    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    public TechBlogCommentLikeJpaEntity(Long commentId, Long memberId) {
        this.commentId = commentId;
        this.memberId = memberId;
    }
}
