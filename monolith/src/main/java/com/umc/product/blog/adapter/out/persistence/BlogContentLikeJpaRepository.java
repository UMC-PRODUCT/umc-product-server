package com.umc.product.blog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.blog.domain.BlogContentLike;
import com.umc.product.blog.domain.BlogContentLikeId;

public interface BlogContentLikeJpaRepository
    extends JpaRepository<BlogContentLike, BlogContentLikeId> {

    boolean existsByContentIdAndMemberId(Long contentId, Long memberId);

    int countByContentId(Long contentId);

    void deleteByContentIdAndMemberId(Long contentId, Long memberId);
}
