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
 * 폴링 대상 Figma 파일과 sync 상태.
 * 마지막으로 처리한 comment 식별자를 보관해 다음 폴링에서 신규 댓글만 필터링한다.
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

    @Column(name = "last_synced_comment_id", length = 100)
    private String lastSyncedCommentId;

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

    public void markSynced(String latestCommentId, Instant syncedAt) {
        this.lastSyncedCommentId = latestCommentId;
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
