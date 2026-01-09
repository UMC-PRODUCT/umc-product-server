package com.umc.product.organization.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private StudyGroup studyGroup;

    // DDD 원칙: 다른 도메인(challenger)의 Entity를 직접 참조하지 않고 ID만 저장
    @Column(nullable = false)
    private Long challengerId;

    @Builder
    private StudyGroupMember(StudyGroup studyGroup, Long challengerId) {
        validate(studyGroup, challengerId);
        this.studyGroup = studyGroup;
        this.challengerId = challengerId;
    }

    private static void validate(StudyGroup studyGroup, Long challengerId) {
        if (studyGroup == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.STUDY_GROUP_REQUIRED);
        }
        if (challengerId == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.CHALLENGER_ID_REQUIRED);
        }
    }
}
