package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
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

    @Column
    private Long scheduleId;

    @Builder(access = AccessLevel.PRIVATE)
    private ChallengerWorkbook(
        Long challengerId, Long originalWorkbookId,
        Long scheduleId, WorkbookStatus status
    ) {
        this.challengerId = challengerId;
        this.originalWorkbookId = originalWorkbookId;
        this.scheduleId = scheduleId;
        this.status = status != null ? status : WorkbookStatus.PENDING;
    }

    public static ChallengerWorkbook create(
        Long challengerId, Long originalWorkbookId,
        WorkbookStatus status, Long scheduleId
    ) {
        return ChallengerWorkbook.builder()
            .challengerId(challengerId)
            .originalWorkbookId(originalWorkbookId)
            .scheduleId(scheduleId)
            .status(status)
            .build();
    }

    /**
     * 워크북 제출 (PENDING → SUBMITTED)
     */
    public void submit() {
        validatePendingStatus();
        this.status = WorkbookStatus.SUBMITTED;
    }

    /**
     * 워크북 심사 (OR 정책: 한 명이라도 PASS하면 PASS)
     * <p>
     * SUBMITTED, PASS, FAIL 상태에서 심사 가능합니다.
     * 이미 PASS인 경우 FAIL로 되돌리지 않습니다.
     *
     * @param status 심사 결과 (PASS 또는 FAIL)
     */
    public void review(WorkbookStatus status) {
        validateCanReview();
        if (this.status == WorkbookStatus.PASS && status == WorkbookStatus.FAIL) {
            return;
        }
        this.status = status;
    }

    /**
     * 베스트 워크북 선정 (SUBMITTED 또는 PASS 상태여야 함)
     */
    public void selectBest() {
        validateCanSelectBest();
        this.status = WorkbookStatus.BEST;
    }

    private void validatePendingStatus() {
        if (this.status != WorkbookStatus.PENDING) {
            throw new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_ALREADY_SUBMITTED);
        }
    }

    private void validateCanReview() {
        if (this.status != WorkbookStatus.SUBMITTED
            && this.status != WorkbookStatus.PASS
            && this.status != WorkbookStatus.FAIL) {
            throw new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_NOT_REVIEWABLE);
        }
    }

    private void validateCanSelectBest() {
        if (this.status != WorkbookStatus.SUBMITTED && this.status != WorkbookStatus.PASS) {
            throw new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_NOT_SELECTABLE_FOR_BEST);
        }
    }
}
