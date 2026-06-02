package com.umc.product.techblog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogCommentLikeId;
import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogCommentLikeJpaEntity;

public interface TechBlogCommentLikeJpaRepository
    extends JpaRepository<TechBlogCommentLikeJpaEntity, TechBlogCommentLikeId> {

    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);

    void deleteByCommentIdAndMemberId(Long commentId, Long memberId);

    void deleteByCommentId(Long commentId);

    int countByCommentId(Long commentId);
}
