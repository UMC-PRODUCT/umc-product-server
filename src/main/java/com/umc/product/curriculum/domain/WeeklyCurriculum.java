package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "weekly_curriculum")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyCurriculum extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id")
    private Curriculum curriculum;

    @Column(name = "is_extra", nullable = false)
    private boolean isExtra;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "week_no", nullable = false)
    private Long weekNo;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Builder(access = AccessLevel.PRIVATE)
    private WeeklyCurriculum(Curriculum curriculum, boolean isExtra, String title, Long weekNo, Instant startsAt, Instant endsAt) {
        validateStartBeforeEnd();
        this.curriculum = curriculum;
        this.isExtra = isExtra;
        this.title = title;
        this.weekNo = weekNo;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    private void validateStartBeforeEnd() {
        if (startsAt.isAfter(endsAt)) {
            throw new CurriculumDomainException(CurriculumErrorCode.INVALID_WEEKLY_CURRICULUM_PERIOD);
        }
    }

    // originalWorkbook 단위로 배포할지 주차단위로 배포할지 고민 -> orginal workbook 단위로 배포하는 것을 원칙으로 함. 이때, 주차별은 서비스단에서 제공하는 부가적인 기능을 제공

}
