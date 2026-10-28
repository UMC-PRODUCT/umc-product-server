package com.umc.product.report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 신고 시점의 대상 정보입니다.
 * <p>
 * 신고 이후 대상이 수정되거나 삭제되어도 운영자가 신고 당시 내용을 확인할 수 있도록 남깁니다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportTargetSnapshot {

    private static final int CONTEXT_MAX_LENGTH = 255;

    /** 신고 대상 작성자의 {@code member.id}. 작성자가 없는 대상(예: 시스템 메시지)은 {@code null}. */
    @Column(name = "target_author_id")
    private Long authorId;

    @Column(name = "target_content", columnDefinition = "TEXT")
    private String content;

    /** 신고 대상이 있던 위치 설명 (예: 스레드 제목). */
    @Column(name = "target_context", length = CONTEXT_MAX_LENGTH)
    private String context;

    private ReportTargetSnapshot(Long authorId, String content, String context) {
        this.authorId = authorId;
        this.content = content;
        this.context = context;
    }

    public static ReportTargetSnapshot of(Long authorId, String content, String context) {
        if (authorId != null && authorId <= 0) {
            throw new IllegalArgumentException("authorId must be positive");
        }
        if (context != null && context.length() > CONTEXT_MAX_LENGTH) {
            throw new IllegalArgumentException("context must not exceed " + CONTEXT_MAX_LENGTH + " characters");
        }
        return new ReportTargetSnapshot(authorId, content, context);
    }
}
