package com.umc.product.organization.domain;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "study_group_mentor")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyGroupMentor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id", nullable = false)
    private StudyGroup studyGroup;

    @Column(nullable = false)
    private Long memberId;

    @Builder(access = AccessLevel.PRIVATE)
    private StudyGroupMentor(StudyGroup studyGroup, Long memberId) {
        this.studyGroup = studyGroup;
        this.memberId = memberId;
    }

    public static StudyGroupMentor create(StudyGroup studyGroup, Long memberId) {
        return StudyGroupMentor.builder()
            .studyGroup(studyGroup)
            .memberId(memberId)
            .build();
    }

}
