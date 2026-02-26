package com.umc.product.recruitment.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "application_part_preference")
public class ApplicationPartPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruitment_part_id", nullable = false)
    private RecruitmentPart recruitmentPart;

    @Column(nullable = false)
    private Integer priority;

    public static ApplicationPartPreference create(
        Application application,
        RecruitmentPart recruitmentPart,
        int priority
    ) {
        return ApplicationPartPreference.builder()
            .application(application)
            .recruitmentPart(recruitmentPart)
            .priority(priority)
            .build();
    }
}
