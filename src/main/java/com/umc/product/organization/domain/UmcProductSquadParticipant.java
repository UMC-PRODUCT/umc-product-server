package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.organization.domain.enums.UmcProductPosition;
import com.umc.product.organization.domain.enums.UmcProductSquadRole;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "umc_product_squad_participant",
    indexes = {
        @Index(name = "ix_umc_product_squad_participant_squad", columnList = "umc_product_squad_id"),
        @Index(name = "ix_umc_product_squad_participant_member", columnList = "umc_product_member_id")
    }
)
public class UmcProductSquadParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "umc_product_squad_id", nullable = false)
    private UmcProductSquad squad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "umc_product_member_id", nullable = false)
    private UmcProductMember umcProductMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UmcProductSquadRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UmcProductPosition position;

    @Column(name = "responsibility_title", length = 200)
    private String responsibilityTitle;

    @Column(name = "responsibility_description", length = 1000)
    private String responsibilityDescription;

    @Builder(access = AccessLevel.PRIVATE)
    private UmcProductSquadParticipant(
        UmcProductSquad squad,
        UmcProductMember umcProductMember,
        UmcProductSquadRole role,
        UmcProductPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        validate(squad, umcProductMember, role, position);
        this.squad = squad;
        this.umcProductMember = umcProductMember;
        this.role = role;
        this.position = position;
        this.responsibilityTitle = normalizeNullable(responsibilityTitle);
        this.responsibilityDescription = normalizeNullable(responsibilityDescription);
    }

    public static UmcProductSquadParticipant create(
        UmcProductSquad squad,
        UmcProductMember umcProductMember,
        UmcProductSquadRole role,
        UmcProductPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        return UmcProductSquadParticipant.builder()
            .squad(squad)
            .umcProductMember(umcProductMember)
            .role(role)
            .position(position)
            .responsibilityTitle(responsibilityTitle)
            .responsibilityDescription(responsibilityDescription)
            .build();
    }

    private static void validate(
        UmcProductSquad squad,
        UmcProductMember umcProductMember,
        UmcProductSquadRole role,
        UmcProductPosition position
    ) {
        if (squad == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_SQUAD_REQUIRED);
        }
        if (umcProductMember == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_MEMBER_REQUIRED);
        }
        if (role == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_ROLE_REQUIRED);
        }
        if (position == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_POSITION_REQUIRED);
        }
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
