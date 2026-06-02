package com.umc.product.techblog.adapter.out.persistence;

import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogCommentJpaEntity;
import com.umc.product.techblog.domain.TechBlogCommentDeletionType;
import com.umc.product.techblog.domain.TechBlogCommentSort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TechBlogCommentQueryRepository {

    private final EntityManager entityManager;
    private final TechBlogCommentJpaRepository commentJpaRepository;

    public List<TechBlogCommentJpaEntity> listTopLevel(
        Long contentId,
        TechBlogCommentSort sort,
        Long cursor,
        int limit
    ) {
        TechBlogCommentJpaEntity cursorEntity = cursor == null
            ? null
            : commentJpaRepository.findByIdAndContentIdAndParentCommentIdIsNull(cursor, contentId).orElse(null);

        String cursorCondition = cursorEntity == null
            ? ""
            : sort.isAscending()
                ? "AND (c.createdAt > :cursorCreatedAt OR (c.createdAt = :cursorCreatedAt AND c.id > :cursorId)) "
                : "AND (c.createdAt < :cursorCreatedAt OR (c.createdAt = :cursorCreatedAt AND c.id < :cursorId)) ";

        String orderBy = sort.isAscending()
            ? "ORDER BY c.createdAt ASC, c.id ASC"
            : "ORDER BY c.createdAt DESC, c.id DESC";

        TypedQuery<TechBlogCommentJpaEntity> query = entityManager.createQuery("""
                SELECT c
                FROM TechBlogCommentJpaEntity c
                WHERE c.contentId = :contentId
                  AND c.parentCommentId IS NULL
                  AND (
                    c.deletionType = :none
                    OR EXISTS (
                      SELECT 1
                      FROM TechBlogCommentJpaEntity r
                      WHERE r.parentCommentId = c.id
                        AND r.deletionType = :none
                    )
                  )
                """ + cursorCondition + orderBy, TechBlogCommentJpaEntity.class)
            .setParameter("contentId", contentId)
            .setParameter("none", TechBlogCommentDeletionType.NONE)
            .setMaxResults(limit);

        if (cursorEntity != null) {
            Instant createdAt = cursorEntity.getCreatedAt();
            query.setParameter("cursorCreatedAt", createdAt);
            query.setParameter("cursorId", cursorEntity.getId());
        }

        return query.getResultList();
    }
}
