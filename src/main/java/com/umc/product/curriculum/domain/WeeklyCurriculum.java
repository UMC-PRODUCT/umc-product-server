package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weekly_curriculum")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyCurriculum extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Curriculum curriculum;

    @Column(name = "is_extra", nullable = false)
    private boolean isExtra;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "week_no", nullable = false)
    private Integer weekNo;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Builder(access = AccessLevel.PRIVATE)
    private WeeklyCurriculum(Curriculum curriculum, boolean isExtra, String title, Integer weekNo, Instant startsAt, Instant endsAt) {
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

    // originalWorkbook 단위로 배포할지 주차단위로 배포할지 고민
    public void release() {

    }

}
