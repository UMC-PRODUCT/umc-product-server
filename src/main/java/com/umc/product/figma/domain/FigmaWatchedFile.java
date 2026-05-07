package com.umc.product.figma.domain;

import com.umc.product.common.BaseEntity;
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
 * 폴링 대상 Figma 파일과 fetch 모니터링 메타데이터 (ADR-003 §Decision 2 + ADR-004 §Decision 5).
 * <p>
 * 시간창 시맨틱 도입 (ADR-004) 이후 본 엔티티는 "어디까지 발송했는가" 를 더 이상 보관하지 않는다. 그 책임은 figma_summary_cursor / figma_comment_dispatch 가 담당하며, 본
 * 엔티티의 last_synced_at / last_error 는 fetch 모니터링 메트릭으로만 의미를 가진다.
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "figma_watched_file")
public class FigmaWatchedFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_key", nullable = false, length = 100)
    private String fileKey;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public static FigmaWatchedFile of(String fileKey, String displayName) {
        return FigmaWatchedFile.builder()
            .fileKey(fileKey)
            .displayName(displayName)
            .enabled(true)
            .build();
    }

    /**
     * fetch 가 정상 완료된 시각을 기록한다 (ADR-004 §Decision 5 이후 last_synced_at / last_error 만 갱신).
     */
    public void markFetched(Instant syncedAt) {
        this.lastSyncedAt = syncedAt;
        this.lastError = null;
    }

    public void recordError(String error) {
        this.lastError = error;
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public void rename(String displayName) {
        this.displayName = displayName;
    }
}
