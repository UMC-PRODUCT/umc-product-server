package com.umc.product.figma.application.port.out;

import java.util.Collection;
import java.util.Set;

/**
 * 댓글 단위 발송 기록을 조회한다 (ADR-004 §Decision 3).
 * <p>
 * 시간창 안에 들어온 댓글 중 이미 dispatch 행이 있는 commentId 를 골라내어, sync / 비-force digest 가 같은 댓글을 두 번 보내지 않도록 한다.
 */
public interface LoadFigmaCommentDispatchPort {

    /**
     * @return 입력 commentId 들 중 dispatch 행이 존재하는 commentId 의 집합. 존재하지 않는 commentId 는 결과에 포함되지 않는다.
     */
    Set<String> findDispatchedCommentIds(Collection<String> commentIds);
}
