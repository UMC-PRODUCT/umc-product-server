package com.umc.product.recruitment.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "recruitment_part")
public class RecruitmentPart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recruitment_id", nullable = false)
    private Long recruitmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengerPart part;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitmentPartStatus status = RecruitmentPartStatus.OPEN;

    public static RecruitmentPart createOpen(Long recruitmentId, ChallengerPart part) {

        validate(recruitmentId, part);

        RecruitmentPart recruitmentPart = new RecruitmentPart();
        recruitmentPart.recruitmentId = recruitmentId;
        recruitmentPart.part = part;
        recruitmentPart.status = RecruitmentPartStatus.OPEN;

        return recruitmentPart;
    }

    public static RecruitmentPart createClosed(Long recruitmentId, ChallengerPart part) {

        validate(recruitmentId, part);

        RecruitmentPart recruitmentPart = new RecruitmentPart();
        recruitmentPart.recruitmentId = recruitmentId;
        recruitmentPart.part = part;
        recruitmentPart.status = RecruitmentPartStatus.CLOSED;

        return recruitmentPart;
    }

    private static void validate(Long recruitmentId, ChallengerPart part) {
        if (recruitmentId == null || part == null) {
            throw new IllegalStateException("recruitmentId/part must not be null");
        }
    }

    public boolean isOpen() {
        return this.status == RecruitmentPartStatus.OPEN;
    }

    public boolean isClosed() {
        return this.status == RecruitmentPartStatus.CLOSED;
    }

}
