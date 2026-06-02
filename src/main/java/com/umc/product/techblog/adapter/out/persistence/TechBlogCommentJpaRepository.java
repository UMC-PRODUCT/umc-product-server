package com.umc.product.techblog.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.techblog.domain.TechBlogComment;

public interface TechBlogCommentJpaRepository extends JpaRepository<TechBlogComment, Long> {

    Optional<TechBlogComment> findByIdAndContentId(Long id, Long contentId);

    Optional<TechBlogComment> findByIdAndContentIdAndParentCommentIdIsNull(Long id, Long contentId);
}
