package com.umc.product.recruitment.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
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

    @Column(name = "doc_score")
    private Integer docScore;

    @Column(name = "interview_score")
    private Integer interviewScore;

    @Column(name = "final_score")
    private Integer finalScore;

    public static Application createApplied(Recruitment recruitment, Long applicantMemberId, Long formResponseId) {
        return Application.builder()
                .recruitment(recruitment)
                .applicantMemberId(applicantMemberId)
                .formResponseId(formResponseId)
                .status(com.umc.product.recruitment.domain.enums.ApplicationStatus.APPLIED)
                .build();
    }

}
