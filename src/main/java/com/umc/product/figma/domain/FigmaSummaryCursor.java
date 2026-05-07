package com.umc.product.figma.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 시간창 기반 figma 댓글 동기화의 단일 행 전역 cursor (ADR-004 §Decision 4).
 * <p>
 * 스케줄러는 매 사이클 (last_window_end, now] 시간창을 처리하고, 발송 성공 시 last_window_end 를 now 로 advance 한다. 다중 인스턴스 환경에서는 SELECT FOR
 * UPDATE 또는 ShedLock 으로 직렬화하며, 단일 row 불변은 application 코드가 보장한다.
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "figma_summary_cursor")
public class FigmaSummaryCursor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_window_end", nullable = false)
    private Instant lastWindowEnd;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * cursor 부재 시 최초 1회 호출되는 부트스트랩 팩토리. 안전 fallback 으로 (now - pollInterval × N) 시각을 initialEnd 로 받는다.
     */
    public static FigmaSummaryCursor bootstrap(Instant initialEnd) {
        return FigmaSummaryCursor.builder()
            .lastWindowEnd(initialEnd)
            .build();
    }

    public void advance(Instant newEnd) {
        if (newEnd == null) {
            return;
        }
        if (this.lastWindowEnd != null && newEnd.isBefore(this.lastWindowEnd)) {
            // cursor 가 과거로 돌아가면 같은 시간창이 다시 발송될 수 있으므로 거절한다.
            return;
        }
        this.lastWindowEnd = newEnd;
    }
}
