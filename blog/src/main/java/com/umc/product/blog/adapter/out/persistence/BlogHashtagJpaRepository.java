package com.umc.product.blog.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.blog.domain.BlogHashtag;

public interface BlogHashtagJpaRepository extends JpaRepository<BlogHashtag, Long> {

    Optional<BlogHashtag> findBySlug(String slug);

    Optional<BlogHashtag> findByNormalizedName(String normalizedName);
}
