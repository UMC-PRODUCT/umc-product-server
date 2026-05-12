package com.umc.product.figma.adapter.out.persistence;

import com.umc.product.figma.application.port.out.LoadFigmaCommentClassificationPort;
import com.umc.product.figma.application.port.out.SaveFigmaCommentClassificationPort;
import com.umc.product.figma.domain.FigmaCommentClassification;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FigmaCommentClassificationPersistenceAdapter
    implements LoadFigmaCommentClassificationPort, SaveFigmaCommentClassificationPort {

    private final FigmaCommentClassificationJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> findClassifications(Collection<String> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Map.of();
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (FigmaCommentClassification e : repository.findAllByCommentIdIn(commentIds)) {
            map.put(e.getCommentId(), e.getDomainKey());
        }
        return map;
    }

    @Override
    @Transactional
    public void save(String commentId, String domainKey, String provider) {
        if (commentId == null || domainKey == null || provider == null) {
            return;
        }
        if (repository.existsByCommentId(commentId)) {
            return;
        }
        try {
            repository.save(FigmaCommentClassification.of(commentId, domainKey, provider, Instant.now()));
        } catch (DataIntegrityViolationException e) {
            // 동시성 race: 다른 인스턴스/호출이 먼저 저장한 케이스. unique constraint 가 보장하므로 무시.
            log.debug("FigmaCommentClassification 동시 저장 race, 무시: commentId={}", commentId);
        }
    }
}
