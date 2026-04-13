package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "challenger_workbook",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_challenger_workbook_challenger_id_original_workbook_id",
        columnNames = {"challenger_id", "original_workbook_id"}
    )
)
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

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "TEXT")
    private String bestReason;

    @Column(columnDefinition = "TEXT")
    private String submission;

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
     *
     * @param missionType 미션 유형
     * @param submission  제출 링크 (깃허브, 노션 등). PLAIN 타입인 경우 null 허용
     */
    public void submit(MissionType missionType, String submission) {
        validatePendingStatus();
        validateSubmission(missionType, submission);
        this.submission = submission;
        this.status = WorkbookStatus.SUBMITTED;
    }

    /**
     * 워크북 심사 (SUBMITTED → PASS or FAIL)
     *
     * @param status   심사 결과 (PASS 또는 FAIL)
     * @param feedback 심사 피드백
     */
    public void review(WorkbookStatus status, String feedback) {
        validateSubmittedStatus();
        this.status = status;
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

    private void validateSubmission(MissionType missionType, String submission) {
        if (missionType != MissionType.PLAIN && (submission == null || submission.isBlank())) {
            throw new CurriculumDomainException(CurriculumErrorCode.SUBMISSION_REQUIRED);
        }
    }

    private void validatePendingStatus() {
        if (this.status != WorkbookStatus.PENDING) {
            throw new CurriculumDomainException(CurriculumErrorCode.INVALID_WORKBOOK_STATUS);
        }
    }

    private void validateSubmittedStatus() {
        if (this.status != WorkbookStatus.SUBMITTED) {
            throw new CurriculumDomainException(CurriculumErrorCode.INVALID_WORKBOOK_STATUS);
        }
    }

    private void validateCanSelectBest() {
        if (this.status != WorkbookStatus.SUBMITTED && this.status != WorkbookStatus.PASS) {
            throw new CurriculumDomainException(CurriculumErrorCode.INVALID_WORKBOOK_STATUS);
        }
    }
}
