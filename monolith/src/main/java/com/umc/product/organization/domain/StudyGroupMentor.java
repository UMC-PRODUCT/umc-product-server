package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
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
        validate(studyGroup, memberId);
        this.studyGroup = studyGroup;
        this.memberId = memberId;
    }

    static StudyGroupMentor create(StudyGroup studyGroup, Long memberId) {
        return StudyGroupMentor.builder()
            .studyGroup(studyGroup)
            .memberId(memberId)
            .build();
    }

    private static void validate(StudyGroup studyGroup, Long memberId) {
        if (studyGroup == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_REQUIRED);
        }
        if (memberId == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MENTOR_ID_REQUIRED);
        }
    }

    boolean isSameMember(Long memberId) {
        return Objects.equals(this.memberId, memberId);
    }
}
