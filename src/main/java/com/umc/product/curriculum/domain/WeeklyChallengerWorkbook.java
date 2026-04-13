package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weekly_challenger_workbook")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyChallengerWorkbook extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_curriculum_id", nullable = false)
    private WeeklyCurriculum weeklyCurriculum;

    private Long memberId;

}
