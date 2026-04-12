package com.umc.product.project.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "project_part_quota",
    // 한 프로젝트 내에서 특정 파트에 대한 TO는 유일해야 합니다.
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_project_part_quota_project_part",
            columnNames = {"project_id", "part"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectPartQuota extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // 어떤 프로젝트의 TO인지

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengerPart part;

    @Column(nullable = false)
    private Long quota;

    private Long lastEditedMemberId;
}
