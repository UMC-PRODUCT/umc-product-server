package com.umc.product.recruitment.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
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
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "application")
public class Application extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruitment_id", nullable = false)
    private Recruitment recruitment;

    @Column(name = "applicant_member_id", nullable = false)
    private Long applicantMemberId;

    @Column(name = "form_response_id", nullable = false)
    private Long formResponseId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_part")
    private ChallengerPart selectedPart;

    // 운영진의 평가 점수를 평균내어 저장하는 용도
    @Column(name = "doc_score")
    private BigDecimal docScore;

    @Column(name = "interview_score")
    private BigDecimal interviewScore;

    // 서류 점수 + 면접 점수로 평균내서 저장. 별도의 컬럼으로 저장해놓을 필요가 있을지 검토 필요.
    @Column(name = "final_score")
    private BigDecimal finalScore;

    public static Application createApplied(Recruitment recruitment, Long applicantMemberId, Long formResponseId) {
        return Application.builder()
            .recruitment(recruitment)
            .applicantMemberId(applicantMemberId)
            .formResponseId(formResponseId)
            .status(com.umc.product.recruitment.domain.enums.ApplicationStatus.APPLIED)
            .build();
    }

    public void passDocument() {
        this.status = ApplicationStatus.DOC_PASSED;
    }

    public void failDocument() {
        this.status = ApplicationStatus.DOC_FAILED;
    }

    public void resetDocumentDecision() {
        this.status = ApplicationStatus.APPLIED;
    }

    public void passFinal(ChallengerPart selectedPart) {
        if (selectedPart == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.FINAL_SELECTED_PART_REQUIRED);
        }
        this.status = ApplicationStatus.FINAL_ACCEPTED;
        this.selectedPart = selectedPart;
    }

    public void failFinal() {
        this.status = ApplicationStatus.FINAL_REJECTED;
        this.selectedPart = null;
    }

    public void resetFinalDecision() {
        // 최종 합격 취소 시 결정 전 상태로 돌아감
        this.status = ApplicationStatus.DOC_PASSED;
        this.selectedPart = null;
    }

}
