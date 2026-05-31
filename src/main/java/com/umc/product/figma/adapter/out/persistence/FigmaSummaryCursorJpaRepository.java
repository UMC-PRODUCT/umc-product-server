package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.domain.FigmaSummaryCursor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FigmaSummaryCursorJpaRepository extends JpaRepository<FigmaSummaryCursor, Long> {

    Optional<FigmaSummaryCursor> findFirstByOrderByIdAsc();
}
