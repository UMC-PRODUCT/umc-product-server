package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.domain.FigmaCommentClassification;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FigmaCommentClassificationJpaRepository extends JpaRepository<FigmaCommentClassification, Long> {

    List<FigmaCommentClassification> findAllByCommentIdIn(Collection<String> commentIds);

    boolean existsByCommentId(String commentId);
}
