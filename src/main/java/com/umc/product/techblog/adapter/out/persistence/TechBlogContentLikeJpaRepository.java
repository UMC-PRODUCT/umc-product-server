package com.umc.product.techblog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogContentLikeId;
import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogContentLikeJpaEntity;

public interface TechBlogContentLikeJpaRepository
    extends JpaRepository<TechBlogContentLikeJpaEntity, TechBlogContentLikeId> {

    boolean existsByContentIdAndMemberId(Long contentId, Long memberId);

    int countByContentId(Long contentId);

    void deleteByContentIdAndMemberId(Long contentId, Long memberId);
}
