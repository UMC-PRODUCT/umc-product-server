package com.umc.product.figma.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Figma 댓글 단위 발송 기록 (ADR-004 §Decision 3).
 * <p>
 * 시간창 시맨틱으로 옮기면서 사라진 "이미 발송된 댓글을 다시 보내지 않는다" 가드 책임을 본 엔티티가 담당한다. (comment_id) 에 unique 제약이 걸려 있어 같은 commentId 가 두 도메인
 * 묶음에 동시에 들어가는 사고를 DB 단에서도 막는다.
 * <p>
 * admin digest 는 force=true 로 본 테이블을 무시하고 재발송할 수 있다. 90일 보존이며 별도 회수 잡으로 정리한다 (ADR-004 §Implementation Plan §7).
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "figma_comment_dispatch")
public class FigmaCommentDispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false, length = 255)
    private String commentId;

    @Column(name = "domain_id", nullable = false)
    private Long domainId;

    @Column(name = "dispatched_at", nullable = false)
    private Instant dispatchedAt;

    public static FigmaCommentDispatch of(String commentId, Long domainId, Instant dispatchedAt) {
        return FigmaCommentDispatch.builder()
            .commentId(commentId)
            .domainId(domainId)
            .dispatchedAt(dispatchedAt)
            .build();
    }
}
