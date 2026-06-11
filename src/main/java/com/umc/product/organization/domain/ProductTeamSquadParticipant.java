package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamSquadRole;
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
    name = "product_team_squad_participant",
    indexes = {
        @Index(name = "ix_product_team_squad_participant_squad", columnList = "product_team_squad_id"),
        @Index(name = "ix_product_team_squad_participant_member", columnList = "product_team_member_id")
    }
)
public class ProductTeamSquadParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_team_squad_id", nullable = false)
    private ProductTeamSquad squad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_team_member_id", nullable = false)
    private ProductTeamMember productTeamMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProductTeamSquadRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProductTeamPosition position;

    @Column(name = "responsibility_title", length = 200)
    private String responsibilityTitle;

    @Column(name = "responsibility_description", length = 1000)
    private String responsibilityDescription;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductTeamSquadParticipant(
        ProductTeamSquad squad,
        ProductTeamMember productTeamMember,
        ProductTeamSquadRole role,
        ProductTeamPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        validate(squad, productTeamMember, role, position);
        this.squad = squad;
        this.productTeamMember = productTeamMember;
        this.role = role;
        this.position = position;
        this.responsibilityTitle = normalizeNullable(responsibilityTitle);
        this.responsibilityDescription = normalizeNullable(responsibilityDescription);
    }

    public static ProductTeamSquadParticipant create(
        ProductTeamSquad squad,
        ProductTeamMember productTeamMember,
        ProductTeamSquadRole role,
        ProductTeamPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        return ProductTeamSquadParticipant.builder()
            .squad(squad)
            .productTeamMember(productTeamMember)
            .role(role)
            .position(position)
            .responsibilityTitle(responsibilityTitle)
            .responsibilityDescription(responsibilityDescription)
            .build();
    }

    private static void validate(
        ProductTeamSquad squad,
        ProductTeamMember productTeamMember,
        ProductTeamSquadRole role,
        ProductTeamPosition position
    ) {
        if (squad == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_SQUAD_REQUIRED);
        }
        if (productTeamMember == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_MEMBER_REQUIRED);
        }
        if (role == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ROLE_REQUIRED);
        }
        if (position == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_POSITION_REQUIRED);
        }
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
