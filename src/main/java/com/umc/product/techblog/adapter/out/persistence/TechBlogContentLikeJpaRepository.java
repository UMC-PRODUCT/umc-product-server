package com.umc.product.techblog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.techblog.domain.TechBlogContentLike;
import com.umc.product.techblog.domain.TechBlogContentLikeId;

public interface TechBlogContentLikeJpaRepository
    extends JpaRepository<TechBlogContentLike, TechBlogContentLikeId> {

    boolean existsByContentIdAndMemberId(Long contentId, Long memberId);

    int countByContentId(Long contentId);

    void deleteByContentIdAndMemberId(Long contentId, Long memberId);
}
