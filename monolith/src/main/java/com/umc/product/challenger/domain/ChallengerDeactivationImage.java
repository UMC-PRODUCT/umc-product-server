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
    name = "challenger_deactivation_image",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_challenger_deactivation_image_history_image",
        columnNames = {"deactivation_history_id", "image_id"}
    )
)
public class ChallengerDeactivationImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deactivation_history_id", nullable = false)
    private ChallengerDeactivationHistory addedHistory;

    @Column(name = "image_id", nullable = false)
    private String imageId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder(access = AccessLevel.PRIVATE)
    private ChallengerDeactivationImage(
        ChallengerDeactivationHistory addedHistory,
        String imageId,
        Integer displayOrder
    ) {
        this.addedHistory = addedHistory;
        this.imageId = imageId;
        this.displayOrder = displayOrder;
    }

    public static ChallengerDeactivationImage create(
        ChallengerDeactivationHistory addedHistory,
        String imageId,
        int displayOrder
    ) {
        validateEvidence(addedHistory, imageId, displayOrder);
        return ChallengerDeactivationImage.builder()
            .addedHistory(addedHistory)
            .imageId(imageId.trim())
            .displayOrder(displayOrder)
            .build();
    }

    private static void validateEvidence(
        ChallengerDeactivationHistory addedHistory,
        String imageId,
        int displayOrder
    ) {
        if (addedHistory == null || imageId == null || imageId.isBlank()) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "근거 추가 기록과 이미지 정보는 필수입니다."
            );
        }
        if (!addedHistory.canOwnAddedEvidence()) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "이미지는 탈부 등록 또는 근거 추가 기록에만 연결할 수 있습니다."
            );
        }
        if (displayOrder < 0) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "이미지 표시 순서는 음수일 수 없습니다."
            );
        }
    }
}
