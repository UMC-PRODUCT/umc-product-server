package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.survey.domain.enums.FormStatus;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import jakarta.persistence.*;
import lombok.*;

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

    public static Form createDraft(Long createdMemberId) {
        Form form = new Form();
        form.createdMemberId = createdMemberId;
        form.status = FormStatus.DRAFT;

        return form;
    }

    public static Form createPublished(Long createdMemberId, String title) {
        Form form = new Form();
        form.createdMemberId = createdMemberId;
        form.title = title;
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
}
