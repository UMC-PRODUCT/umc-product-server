package com.umc.product.challenger.domain;

import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "challenger_deactivation_link",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_challenger_deactivation_link_history_link",
        columnNames = {"deactivation_history_id", "link"}
    )
)
public class ChallengerDeactivationLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deactivation_history_id", nullable = false)
    private ChallengerDeactivationHistory addedHistory;

    @Column(name = "link", nullable = false)
    private String link;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder(access = AccessLevel.PRIVATE)
    private ChallengerDeactivationLink(
        ChallengerDeactivationHistory addedHistory,
        String link,
        Integer displayOrder
    ) {
        this.addedHistory = addedHistory;
        this.link = link;
        this.displayOrder = displayOrder;
    }

    public static ChallengerDeactivationLink create(
        ChallengerDeactivationHistory addedHistory,
        String link,
        int displayOrder
    ) {
        validateEvidence(addedHistory, link, displayOrder);
        return ChallengerDeactivationLink.builder()
            .addedHistory(addedHistory)
            .link(link.trim())
            .displayOrder(displayOrder)
            .build();
    }

    private static void validateEvidence(
        ChallengerDeactivationHistory addedHistory,
        String link,
        int displayOrder
    ) {
        if (addedHistory == null || link == null || link.isBlank()) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "근거 추가 기록과 링크 정보는 필수입니다."
            );
        }
        if (!addedHistory.canOwnAddedEvidence()) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "링크는 탈부 등록 또는 근거 추가 기록에만 연결할 수 있습니다."
            );
        }
        if (displayOrder < 0) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "링크 표시 순서는 음수일 수 없습니다."
            );
        }
    }
}
