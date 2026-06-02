package com.umc.product.techblog.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentType;

public interface TechBlogContentJpaRepository extends JpaRepository<TechBlogContent, Long> {

    Optional<TechBlogContent> findByContentTypeAndSlug(TechBlogContentType contentType, String slug);
}
