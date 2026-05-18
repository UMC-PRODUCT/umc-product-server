package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.domain.FigmaCommentDispatch;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FigmaCommentDispatchJpaRepository extends JpaRepository<FigmaCommentDispatch, Long> {

    List<FigmaCommentDispatch> findAllByCommentIdIn(Collection<String> commentIds);

    boolean existsByCommentId(String commentId);

    @Modifying
    @Query("DELETE FROM FigmaCommentDispatch d WHERE d.dispatchedAt < :threshold")
    int deleteAllByDispatchedAtBefore(@Param("threshold") Instant threshold);
}
