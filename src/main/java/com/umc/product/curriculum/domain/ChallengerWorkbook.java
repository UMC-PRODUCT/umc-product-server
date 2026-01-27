package com.umc.product.curriculum.domain;

import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "challenger_workbook")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengerWorkbook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long challengerId;

    @Column(nullable = false)
    private Long originalWorkbookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkbookStatus status;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "TEXT")
    private String bestReason;

    @Column(columnDefinition = "TEXT")
    private String submission;

    @Builder
    private ChallengerWorkbook(Long challengerId, Long originalWorkbookId, Long scheduleId,
                               WorkbookStatus status) {
        this.challengerId = challengerId;
        this.originalWorkbookId = originalWorkbookId;
        this.scheduleId = scheduleId;
        this.status = status != null ? status : WorkbookStatus.PENDING;
    }

    /**
     * 워크북 제출 (PENDING → SUBMITTED)
     *
     * @param submission 제출 링크 (깃허브, 노션 등)
     */
    public void submit(String submission) {
        validatePendingStatus();
        this.submission = submission;
        this.status = WorkbookStatus.SUBMITTED;
    }

    /**
     * 심사 통과 (SUBMITTED → PASS)
     */
    public void markAsPass(String feedback) {
        validateSubmittedStatus();
        this.status = WorkbookStatus.PASS;
        this.feedback = feedback;
    }

    /**
     * 심사 불합격 (SUBMITTED → FAIL)
     */
    public void markAsFail(String feedback) {
        validateSubmittedStatus();
        this.status = WorkbookStatus.FAIL;
        this.feedback = feedback;
    }

    /**
     * 베스트 워크북 선정 (SUBMITTED 또는 PASS 상태여야 함)
     */
    public void selectBest(String bestReason) {
        validateCanSelectBest();
        this.status = WorkbookStatus.BEST;
        this.bestReason = bestReason;
    }

    private void validatePendingStatus() {
        if (this.status != WorkbookStatus.PENDING) {
            throw new BusinessException(Domain.CHALLENGER, ChallengerErrorCode.INVALID_WORKBOOK_STATUS);
        }
    }

    private void validateSubmittedStatus() {
        if (this.status != WorkbookStatus.SUBMITTED) {
            throw new BusinessException(Domain.CHALLENGER, ChallengerErrorCode.INVALID_WORKBOOK_STATUS);
        }
    }

    private void validateCanSelectBest() {
        if (this.status != WorkbookStatus.SUBMITTED && this.status != WorkbookStatus.PASS) {
            throw new BusinessException(Domain.CHALLENGER, ChallengerErrorCode.INVALID_WORKBOOK_STATUS);
        }
    }
}
