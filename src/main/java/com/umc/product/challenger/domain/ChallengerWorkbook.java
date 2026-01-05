package com.umc.product.challenger.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.WorkbookStatus;
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

    @Column(nullable = false)
    private Boolean isBest;

    @Builder
    private ChallengerWorkbook(Long challengerId, Long originalWorkbookId, Long scheduleId) {
        this.challengerId = challengerId;
        this.originalWorkbookId = originalWorkbookId;
        this.scheduleId = scheduleId;
        this.status = WorkbookStatus.PENDING;
        this.isBest = false;
    }

//    public void markAsPass() {
//        this.status = WorkbookStatus.PASS;
//    }
//
//    public void markAsFail() {
//        this.status = WorkbookStatus.FAIL;
//    }
//
//    public void selectAsBest() {
//        validatePassStatus();
//        this.isBest = true;
//    }
//
//    public void unselectAsBest() {
//        this.isBest = false;
//    }
//
//    private void validatePassStatus() {
//        if (this.status != WorkbookStatus.PASS) {
//            throw new BusinessException(ErrorCode.INVALID_WORKBOOK_STATUS);
//        }
//    }
}
