package com.umc.product.community.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentJpaEntity, Long> {

    Page<CommentJpaEntity> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    int countByPostId(Long postId);
}
