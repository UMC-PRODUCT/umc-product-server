package com.umc.product.techblog.adapter.out.persistence;

import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogCommentLikeId;
import com.umc.product.techblog.adapter.out.persistence.entity.TechBlogCommentLikeJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TechBlogCommentLikeJpaRepository
    extends JpaRepository<TechBlogCommentLikeJpaEntity, TechBlogCommentLikeId> {

    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);

    void deleteByCommentIdAndMemberId(Long commentId, Long memberId);

    void deleteByCommentId(Long commentId);

    int countByCommentId(Long commentId);

    @Query("""
        SELECT l.commentId, COUNT(l)
        FROM TechBlogCommentLikeJpaEntity l
        WHERE l.commentId IN :commentIds
        GROUP BY l.commentId
        """)
    List<Object[]> countByCommentIds(@Param("commentIds") List<Long> commentIds);

    @Query("""
        SELECT l.commentId
        FROM TechBlogCommentLikeJpaEntity l
        WHERE l.commentId IN :commentIds
          AND l.memberId = :memberId
        """)
    List<Long> findLikedCommentIds(@Param("commentIds") List<Long> commentIds, @Param("memberId") Long memberId);
}
