package com.umc.product.blog.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.blog.domain.BlogComment;

public interface BlogCommentJpaRepository extends JpaRepository<BlogComment, Long> {

    Optional<BlogComment> findByIdAndContentId(Long id, Long contentId);

    Optional<BlogComment> findByIdAndContentIdAndParentCommentIdIsNull(Long id, Long contentId);
}
