package com.umc.product.figma.application.port.out;

import java.time.Instant;
import java.util.Collection;

/**
 * 댓글 단위 발송 기록을 저장/회수한다 (ADR-004 §Decision 3, §Implementation Plan §7).
 */
public interface SaveFigmaCommentDispatchPort {

    /**
     * 한 도메인 묶음의 발송이 성공한 후, 묶음 내 commentId 들에 대한 dispatch 행을 일괄 insert 한다.
     * 이미 같은 commentId 의 dispatch 행이 있으면 unique 제약 race 로 간주하고 조용히 건너뛴다.
     */
    void recordDispatched(Collection<String> commentIds, Long domainId, Instant dispatchedAt);

    /**
     * 회수 잡 (ADR-004 §Implementation Plan §7) — dispatched_at &lt; threshold 인 행을 삭제한다.
     *
     * @return 삭제된 행 수
     */
    int deleteOlderThan(Instant threshold);
}
