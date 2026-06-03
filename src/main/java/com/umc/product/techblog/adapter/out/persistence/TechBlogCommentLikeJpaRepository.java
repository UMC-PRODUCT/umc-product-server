package com.umc.product.techblog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.techblog.domain.TechBlogCommentLike;
import com.umc.product.techblog.domain.TechBlogCommentLikeId;

public interface TechBlogCommentLikeJpaRepository
    extends JpaRepository<TechBlogCommentLike, TechBlogCommentLikeId> {

    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);

    void deleteByCommentIdAndMemberId(Long commentId, Long memberId);

    void deleteByCommentId(Long commentId);

    int countByCommentId(Long commentId);
}
