package com.umc.product.organization.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.exception.OrganizationErrorCode;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private StudyGroup studyGroup;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
//    private Challenger challenger;


    // TODO: Challenger 추가시 생성자에 추가
    @Builder
    private StudyGroupMember(StudyGroup group) {
        validate(group /*, challenger*/);
        this.studyGroup = group;
    }

    private static void validate(StudyGroup group /*, Challenger challenger*/) {
        if (group == null) throw new BusinessException(Domain.COMMON, OrganizationErrorCode.STUDY_GROUP_REQUIRED);
        // if (challenger == null) throw new BusinessException(Domain.COMMON, OrganizationErrorCode.STUDY_GROUP_MEMBER_REQUIRED);
    }
}
