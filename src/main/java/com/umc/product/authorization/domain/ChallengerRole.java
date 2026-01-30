package com.umc.product.authorization.domain;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenger_role")
public class ChallengerRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "challenger_id", nullable = false)
    private Challenger challenger;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role_type")
    private ChallengerRoleType challengerRoleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "organization_type")
    private OrganizationType organizationType;

    @Column(nullable = false, name = "organization_id")
    private Long organizationId;

    // 파트장인 경우 어떤 파트의 파트장인지
    // 본인이 활동 중인 파트와 다른 파트의 파트장인 경우가 있어서 명시하도록 함
    @Enumerated(EnumType.STRING)
    @Column(name = "leading_part")
    private ChallengerPart leadingPart;

    @Column(nullable = false, name = "gisu_id")
    private Long gisuId;
}
