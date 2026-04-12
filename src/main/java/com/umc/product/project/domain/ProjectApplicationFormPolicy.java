package com.umc.product.project.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 프로젝트에 지원하기 위한 폼 내에 포함된 특정 섹션이 어떤 파트에 의해서 접근이 가능한지를 나타내는 엔티티입니다.
 * <p>
 * 프로젝트 지원용 폼 조회 시, 본 엔티티를 반드시 거쳐서 접근 권한이 있는 사용자에게만 노출되도록 하여야 하며,
 * <p>
 * Form 내에 있는 FormSection 중에서 본 Policy가 명시되지 않은 Section은 어떠한 파트에게도 노출되지 않습니다.
 * <p>
 * 단, 운영진이나 프로젝트 Plan은 반드시 해당 사항을 조회할 수 있어야 합니다.
 */
@Entity
@Table(name = "project_application_form_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectApplicationFormPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_application_form_id", nullable = false)
    private ProjectApplicationForm applicationForm;

    private Long formSectionId; // 접근 권한 제한을 받을 FormSection의 ID

    @Enumerated(EnumType.STRING) // Enum 값이므로 String으로 저장
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_parts", columnDefinition = "varchar[]")
    private Set<ChallengerPart> allowedParts = new HashSet<>();
}
