package com.umc.product.blog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.blog.domain.BlogContentHashtag;
import com.umc.product.blog.domain.BlogContentHashtagId;

public interface BlogContentHashtagJpaRepository extends JpaRepository<BlogContentHashtag, BlogContentHashtagId> {

    void deleteByContentId(Long contentId);
}
