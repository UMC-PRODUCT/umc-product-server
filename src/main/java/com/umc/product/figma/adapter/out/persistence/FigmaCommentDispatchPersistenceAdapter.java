package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.application.port.out.LoadFigmaCommentDispatchPort;
import com.umc.product.figma.application.port.out.SaveFigmaCommentDispatchPort;
import com.umc.product.figma.domain.FigmaCommentDispatch;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * figma_comment_dispatch 의 영속화 (ADR-004 §Decision 3 / §Implementation Plan §7).
 * <p>
 * 동일 commentId 의 동시 insert 는 unique 제약으로 차단되며, race 시 조용히 건너뛴다 (이미 발송 기록이 있다는 사실 자체는 보존되므로).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FigmaCommentDispatchPersistenceAdapter
    implements LoadFigmaCommentDispatchPort, SaveFigmaCommentDispatchPort {

    private final FigmaCommentDispatchJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Set<String> findDispatchedCommentIds(Collection<String> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Set.of();
        }
        Set<String> result = new HashSet<>();
        for (FigmaCommentDispatch d : repository.findAllByCommentIdIn(commentIds)) {
            result.add(d.getCommentId());
        }
        return result;
    }

    @Override
    @Transactional
    public void recordDispatched(Collection<String> commentIds, Long domainId, Instant dispatchedAt) {
        if (commentIds == null || commentIds.isEmpty() || domainId == null || dispatchedAt == null) {
            return;
        }
        for (String commentId : commentIds) {
            if (commentId == null || repository.existsByCommentId(commentId)) {
                continue;
            }
            try {
                repository.save(FigmaCommentDispatch.of(commentId, domainId, dispatchedAt));
            } catch (DataIntegrityViolationException e) {
                // 동시성 race: 다른 인스턴스/호출이 먼저 저장한 케이스.
                // unique 제약이 보장되어 있으므로 발송 기록 자체는 정상 보존된다.
                log.debug("FigmaCommentDispatch 동시 저장 race, 무시: commentId={}", commentId);
            }
        }
    }

    @Override
    @Transactional
    public int deleteOlderThan(Instant threshold) {
        if (threshold == null) {
            return 0;
        }
        return repository.deleteAllByDispatchedAtBefore(threshold);
    }
}
