package com.umc.product.challenger.domain;

import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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
    private boolean isBest;

    @OneToMany(mappedBy = "challengerWorkbook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengerMission> missions = new ArrayList<>();

    @Builder
    private ChallengerWorkbook(Long challengerId, Long originalWorkbookId, Long scheduleId) {
        this.challengerId = challengerId;
        this.originalWorkbookId = originalWorkbookId;
        this.scheduleId = scheduleId;
        this.status = WorkbookStatus.PENDING;
        this.isBest = false;
    }

    public void markAsPass() {
        validatePendingStatus();
        this.status = WorkbookStatus.PASS;
    }

    public void markAsFail() {
        validatePendingStatus();
        this.status = WorkbookStatus.FAIL;
    }

    public void selectAsBest() {
        validatePassStatus();
        this.isBest = true;
    }

    public void unselectAsBest() {
        this.isBest = false;
    }

    private void validatePassStatus() {
        if (this.status != WorkbookStatus.PASS) {
            throw new BusinessException(Domain.CHALLENGER, ChallengerErrorCode.INVALID_WORKBOOK_STATUS);
        }
    }

    private void validatePendingStatus() {
        if (this.status != WorkbookStatus.PENDING) {
            throw new BusinessException(Domain.CHALLENGER, ChallengerErrorCode.INVALID_WORKBOOK_STATUS);
        }
    }
}
