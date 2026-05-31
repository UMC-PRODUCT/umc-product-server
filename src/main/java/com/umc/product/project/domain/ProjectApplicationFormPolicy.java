package com.umc.product.project.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.FormSectionType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 지원 폼 내 특정 섹션의 가시성 정책.
 * <p>
 * 각 섹션은 {@link FormSectionType#COMMON COMMON} 또는 {@link FormSectionType#PART PART} 중 하나로 분류되며,
 * 챌린저가 폼을 조회할 때 본인 파트가 해당 섹션을 볼 수 있는지 결정한다.
 * <ul>
 *   <li>{@code COMMON} — 모든 파트 노출, {@code allowedParts} 무시</li>
 *   <li>{@code PART} — {@code allowedParts} 명시 1개 이상</li>
 * </ul>
 * 운영진이나 프로젝트 PM 은 본 정책을 우회하고 전체 섹션을 조회한다.
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

    @Column(nullable = false)
    private Long formSectionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormSectionType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_parts", columnDefinition = "text[]")
    private List<ChallengerPart> allowedParts = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private ProjectApplicationFormPolicy(
        ProjectApplicationForm applicationForm,
        Long formSectionId,
        FormSectionType type,
        List<ChallengerPart> allowedParts
    ) {
        this.applicationForm = applicationForm;
        this.formSectionId = formSectionId;
        this.type = type;
        this.allowedParts = allowedParts;
    }

    /**
     * 모든 파트에게 노출되는 공통 섹션 정책을 생성한다.
     */
    public static ProjectApplicationFormPolicy createCommon(
        ProjectApplicationForm form, Long formSectionId
    ) {
        return ProjectApplicationFormPolicy.builder()
            .applicationForm(form)
            .formSectionId(formSectionId)
            .type(FormSectionType.COMMON)
            .allowedParts(new ArrayList<>())
            .build();
    }

    /**
     * 특정 파트에게만 노출되는 섹션 정책을 생성한다.
     * {@code allowedParts} 는 1개 이상이어야 한다.
     */
    public static ProjectApplicationFormPolicy createForParts(
        ProjectApplicationForm form, Long formSectionId, Set<ChallengerPart> allowedParts
    ) {
        if (allowedParts == null || allowedParts.isEmpty()) {
            throw new ProjectDomainException(ProjectErrorCode.APPLICATION_FORM_POLICY_PARTS_EMPTY);
        }
        return ProjectApplicationFormPolicy.builder()
            .applicationForm(form)
            .formSectionId(formSectionId)
            .type(FormSectionType.PART)
            .allowedParts(new ArrayList<>(allowedParts))
            .build();
    }

    /**
     * 정책 타입과 노출 파트 목록을 갱신한다.
     * <p>
     * {@code COMMON} 으로 변경하면 {@code allowedParts} 는 비워진다.
     * {@code PART} 로 변경 시 {@code allowedParts} 가 비어있으면 도메인 예외를 던진다.
     */
    public void updatePolicy(FormSectionType type, Set<ChallengerPart> allowedParts) {
        if (type == FormSectionType.PART && (allowedParts == null || allowedParts.isEmpty())) {
            throw new ProjectDomainException(ProjectErrorCode.APPLICATION_FORM_POLICY_PARTS_EMPTY);
        }
        this.type = type;
        this.allowedParts = (type == FormSectionType.COMMON)
            ? new ArrayList<>()
            : new ArrayList<>(allowedParts);
    }

    /**
     * 특정 파트가 본 섹션을 조회할 수 있는지 판단한다.
     * <p>
     * {@code COMMON} 이면 항상 true (ChallengerPart enum 추가에도 안전).
     * {@code PART} 이면 {@code allowedParts} 매칭.
     */
    public boolean canAccess(ChallengerPart part) {
        if (type == FormSectionType.COMMON) {
            return true;
        }
        return allowedParts.contains(part);
    }

    public void validateSectionAccessPermission(ChallengerPart part) {
        if (canAccess(part)) {
            return;
        }
        throw new ProjectDomainException(ProjectErrorCode.APPLICATION_FORM_ACCESS_NOT_ALLOWED);
    }
}
