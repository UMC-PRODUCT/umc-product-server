package com.umc.product.blog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.blog.domain.BlogCommentLike;
import com.umc.product.blog.domain.BlogCommentLikeId;

public interface BlogCommentLikeJpaRepository
    extends JpaRepository<BlogCommentLike, BlogCommentLikeId> {

    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);

    void deleteByCommentIdAndMemberId(Long commentId, Long memberId);

    void deleteByCommentId(Long commentId);

    int countByCommentId(Long commentId);
}
