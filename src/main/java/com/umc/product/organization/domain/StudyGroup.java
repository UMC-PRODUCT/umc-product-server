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
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gisu_id")
    private Gisu gisu;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "group_leader_id")
//    private Challenger challenger;


    // TODO: Challenger 추가시 생성자에 추가
    @Builder
    private StudyGroup(String name, Gisu gisu) {
        validate(name, gisu /*, challenger*/);
        this.name = name;
        this.gisu = gisu;
    }

    private static void validate(String name, Gisu gisu /*, Challenger challenger*/) {
        if (name == null || name.isBlank()) throw new BusinessException(Domain.COMMON, OrganizationErrorCode.STUDY_GROUP_NAME_REQUIRED);
        if (gisu == null) throw new BusinessException(Domain.COMMON, OrganizationErrorCode.GISU_REQUIRED);
        // if (challenger == null) throw new BusinessException(Domain.COMMON, OrganizationErrorCode.STUDY_GROUP_LEADER_REQUIRED);
    }

}
