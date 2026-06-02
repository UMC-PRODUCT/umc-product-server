package com.umc.product.techblog.adapter.out.persistence;

import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogCommentJpaEntity;
import com.umc.product.techblog.domain.TechBlogCommentDeletionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechBlogCommentJpaRepository extends JpaRepository<TechBlogCommentJpaEntity, Long> {

    Optional<TechBlogCommentJpaEntity> findByIdAndContentId(Long id, Long contentId);

    Optional<TechBlogCommentJpaEntity> findByIdAndContentIdAndParentCommentIdIsNull(Long id, Long contentId);

    boolean existsByParentCommentIdAndDeletionType(Long parentCommentId, TechBlogCommentDeletionType deletionType);

    List<TechBlogCommentJpaEntity> findByParentCommentIdInAndDeletionTypeOrderByCreatedAtAscIdAsc(
        List<Long> parentCommentIds,
        TechBlogCommentDeletionType deletionType
    );
}
