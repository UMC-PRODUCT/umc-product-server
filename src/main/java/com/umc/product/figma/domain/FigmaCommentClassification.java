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
 * Figma 댓글의 LLM 분류 결과 영구 캐시. 동일 commentId 재분류를 막아 재시작/다중 인스턴스 환경에서도 LLM 호출 횟수를 0회로 수렴시킨다.
 * <p>
 * mock provider 응답이나 후보 외 응답은 본 테이블에 저장하지 않고 in-memory 캐시에만 두어, 검증 단계의 임시 분류가 운영에 영구 누적되지 않게 한다.
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "figma_comment_classification")
public class FigmaCommentClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false, length = 255)
    private String commentId;

    @Column(name = "domain_key", nullable = false, length = 100)
    private String domainKey;

    @Column(name = "provider", nullable = false, length = 64)
    private String provider;

    @Column(name = "classified_at", nullable = false)
    private Instant classifiedAt;

    public static FigmaCommentClassification of(String commentId, String domainKey, String provider,
                                                Instant classifiedAt) {
        return FigmaCommentClassification.builder()
            .commentId(commentId)
            .domainKey(domainKey)
            .provider(provider)
            .classifiedAt(classifiedAt)
            .build();
    }
}
