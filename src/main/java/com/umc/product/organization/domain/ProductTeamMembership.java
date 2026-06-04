package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.organization.domain.enums.ProductTeamPart;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamRole;
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
    name = "product_team_membership",
    indexes = {
        @Index(name = "ix_product_team_membership_generation_part", columnList = "product_team_generation_id, part"),
        @Index(name = "ix_product_team_membership_member", columnList = "product_team_member_id")
    }
)
public class ProductTeamMembership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_team_member_id", nullable = false)
    private ProductTeamMember productTeamMember;

    @Column(name = "product_team_generation_id", nullable = false)
    private Long productTeamGenerationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProductTeamPart part;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProductTeamRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProductTeamPosition position;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductTeamMembership(
        ProductTeamMember productTeamMember,
        Long productTeamGenerationId,
        ProductTeamPart part,
        ProductTeamRole role,
        ProductTeamPosition position
    ) {
        validate(productTeamMember, productTeamGenerationId, part, role, position);
        this.productTeamMember = productTeamMember;
        this.productTeamGenerationId = productTeamGenerationId;
        this.part = part;
        this.role = role;
        this.position = position;
    }

    public static ProductTeamMembership create(
        ProductTeamMember productTeamMember,
        Long productTeamGenerationId,
        ProductTeamPart part,
        ProductTeamRole role,
        ProductTeamPosition position
    ) {
        return ProductTeamMembership.builder()
            .productTeamMember(productTeamMember)
            .productTeamGenerationId(productTeamGenerationId)
            .part(part)
            .role(role)
            .position(position)
            .build();
    }

    private static void validate(
        ProductTeamMember productTeamMember,
        Long productTeamGenerationId,
        ProductTeamPart part,
        ProductTeamRole role,
        ProductTeamPosition position
    ) {
        if (productTeamMember == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_MEMBER_REQUIRED);
        }
        if (productTeamGenerationId == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_GENERATION_REQUIRED);
        }
        if (part == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_PART_REQUIRED);
        }
        if (role == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ROLE_REQUIRED);
        }
        if (position == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_POSITION_REQUIRED);
        }
    }
}
