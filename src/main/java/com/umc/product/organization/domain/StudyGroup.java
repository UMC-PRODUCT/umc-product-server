package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_group")
public class StudyGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(nullable = false)
    private Long gisuId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengerPart part;

    @Builder(access = AccessLevel.PRIVATE)
    private StudyGroup(String name, Long gisuId, ChallengerPart part) {
        validate(name, gisuId, part);
        this.name = name;
        this.gisuId = gisuId;
        this.part = part;
    }

    public static StudyGroup create(
        String name, Long gisuId, ChallengerPart part
    ) {
        return StudyGroup.builder()
            .name(name)
            .gisuId(gisuId)
            .part(part)
            .build();
    }

    private static void validate(String name, Long gisuId, ChallengerPart part) {
        if (name == null || name.isBlank()) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_NAME_REQUIRED);
        }

        if (gisuId == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.GISU_REQUIRED);
        }

        if (part == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.PART_REQUIRED);
        }
    }

    // ============ Domain Methods (Aggregate Root Pattern) ============

    public void updateName(String name) {
        if (StringUtils.hasText(name)) {
            this.name = name;
        }
    }

    public void updatePart(ChallengerPart challengerPart) {
        if (challengerPart != null) {
            this.part = challengerPart;
        }
    }
}
