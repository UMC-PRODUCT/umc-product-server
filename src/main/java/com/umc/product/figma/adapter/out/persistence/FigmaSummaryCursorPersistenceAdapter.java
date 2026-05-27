package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.application.port.out.LoadFigmaSummaryCursorPort;
import com.umc.product.figma.application.port.out.SaveFigmaSummaryCursorPort;
import com.umc.product.figma.domain.FigmaSummaryCursor;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * figma_summary_cursor 단일 row 의 영속화 (ADR-004 §Decision 4).
 * <p>
 * application 코드가 단일 row 불변을 보장한다. 본 어댑터는 row 가 0건일 때 빈 Optional 을, 1건일 때 그 row 를 반환한다.
 */
@Component
@RequiredArgsConstructor
public class FigmaSummaryCursorPersistenceAdapter
    implements LoadFigmaSummaryCursorPort, SaveFigmaSummaryCursorPort {

    private final FigmaSummaryCursorJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<FigmaSummaryCursor> findCursor() {
        return repository.findFirstByOrderByIdAsc();
    }

    @Override
    @Transactional
    public FigmaSummaryCursor save(FigmaSummaryCursor cursor) {
        return repository.save(cursor);
    }
}
