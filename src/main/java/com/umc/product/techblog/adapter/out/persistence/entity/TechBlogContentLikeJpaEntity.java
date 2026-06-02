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
@Table(name = "tech_blog_content_like")
@IdClass(TechBlogContentLikeId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechBlogContentLikeJpaEntity {

    @Id
    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    public TechBlogContentLikeJpaEntity(Long contentId, Long memberId) {
        this.contentId = contentId;
        this.memberId = memberId;
    }
}
