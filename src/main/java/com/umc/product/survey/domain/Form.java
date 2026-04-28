package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.survey.domain.enums.FormStatus;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "form")
public class Form extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_member_id", nullable = false)
    private Long createdMemberId;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormStatus status;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    /**
     * Draft 생성을 위해서는 제목은 필수로 입력하여야 합니다.
     */
    public static Form createDraft(String title, Long createdMemberId) {
        Form form = new Form();

        form.title = title;
        form.createdMemberId = createdMemberId;
        form.status = FormStatus.DRAFT;

        return form;
    }

    public static Form createPublished(Long createdMemberId, String title, boolean isAnonymous) {
        Form form = new Form();
        form.createdMemberId = createdMemberId;
        form.title = title;
        form.isAnonymous = isAnonymous;
        form.status = FormStatus.PUBLISHED;

        return form;
    }

    public boolean isPublished() {
        return this.status == FormStatus.PUBLISHED;
    }

    public void publish() {
        if (isPublished()) {
            throw new SurveyDomainException(SurveyErrorCode.SURVEY_ALREADY_PUBLISHED);
        }

        this.status = FormStatus.PUBLISHED;
    }

    /**
     * 폼 메타데이터 부분 업데이트.
     * null 인 필드는 기존 값 유지. 임시저장 단계에서 어느 필드든 부분 변경 가능하도록 모든 파라미터 nullable.
     */
    public void update(String title, String description, Boolean isAnonymous) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (isAnonymous != null) this.isAnonymous = isAnonymous;
    }
}
