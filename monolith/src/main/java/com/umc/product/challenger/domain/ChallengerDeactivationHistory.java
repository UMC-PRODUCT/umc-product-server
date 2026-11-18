package com.umc.product.challenger.domain;

import com.umc.product.challenger.domain.enums.ChallengerDeactivationHistoryAction;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 탈부 등록과 근거 자료 추가 작업을 요청 단위로 보존하는 진행 기록입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenger_deactivation_history")
public class ChallengerDeactivationHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenger_deactivation_id", nullable = false)
    private ChallengerDeactivation deactivation;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ChallengerDeactivationHistoryAction action;

    @Column(name = "image_count", nullable = false)
    private Integer imageCount;

    @Column(name = "link_count", nullable = false)
    private Integer linkCount;

    @Column(name = "actor_member_id", nullable = false)
    private Long actorMemberId;

    @Builder(access = AccessLevel.PRIVATE)
    private ChallengerDeactivationHistory(
        ChallengerDeactivation deactivation,
        ChallengerDeactivationHistoryAction action,
        Integer imageCount,
        Integer linkCount,
        Long actorMemberId
    ) {
        this.deactivation = deactivation;
        this.action = action;
        this.imageCount = imageCount;
        this.linkCount = linkCount;
        this.actorMemberId = actorMemberId;
    }

    public static ChallengerDeactivationHistory create(
        ChallengerDeactivation deactivation,
        ChallengerDeactivationHistoryAction action,
        int imageCount,
        int linkCount,
        Long actorMemberId
    ) {
        if (deactivation == null || action == null || actorMemberId == null) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "탈부 기록, 진행 작업, 작업자 정보는 필수입니다."
            );
        }
        if (imageCount < 0 || linkCount < 0) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "근거 자료 개수는 음수일 수 없습니다."
            );
        }
        if (imageCount > ChallengerDeactivation.MAX_IMAGE_COUNT
            || linkCount > ChallengerDeactivation.MAX_LINK_COUNT) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "근거 자료 등록 가능 개수를 초과했습니다."
            );
        }
        if (action != ChallengerDeactivationHistoryAction.REGISTERED
            && imageCount == 0 && linkCount == 0) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "근거 추가 기록에는 사진이나 링크가 하나 이상 필요합니다."
            );
        }
        return ChallengerDeactivationHistory.builder()
            .deactivation(deactivation)
            .action(action)
            .imageCount(imageCount)
            .linkCount(linkCount)
            .actorMemberId(actorMemberId)
            .build();
    }

    public boolean canOwnAddedEvidence() {
        return action == ChallengerDeactivationHistoryAction.REGISTERED
            || action == ChallengerDeactivationHistoryAction.EVIDENCE_ADDED;
    }
}
