package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.adapter.out.persistence.entity.CommentJpaEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<CommentJpaEntity, Long> {

    Page<CommentJpaEntity> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    int countByPostId(Long postId);

    @Query("SELECT c.postId, COUNT(c) FROM CommentJpaEntity c WHERE c.postId IN :postIds GROUP BY c.postId")
    List<Object[]> countByPostIdIn(@Param("postIds") List<Long> postIds);
}
