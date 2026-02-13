package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.survey.domain.enums.FormOpenStatus;
import com.umc.product.survey.domain.enums.FormSectionType;
import com.umc.product.survey.domain.enums.FormStatus;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

    @Column()
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormStatus status;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    // 시작일 00:00(KST) ~ 마감일 23:59(KST) = (마감일+1) 00:00(KST) exclusive
    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at_exclusive")
    private Instant endsAtExclusive;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNo ASC")
    @Builder.Default
    private Set<FormSection> sections = new LinkedHashSet<>();

    public static Form createDraft(Long createdMemberId) {
        Form form = new Form();
        form.createdMemberId = createdMemberId;
        form.status = FormStatus.DRAFT;
        return form;
    }

    public boolean isPublished() {
        return this.status == FormStatus.PUBLISHED;
    }

    public void publish() {
        if (isPublished()) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_ALREADY_PUBLISHED);
        }
        this.status = FormStatus.PUBLISHED;
    }

    public void setVotePolicy(boolean isAnonymous, Instant startsAt, Instant endsAtExclusive) {
        if (startsAt != null && endsAtExclusive != null && !endsAtExclusive.isAfter(startsAt)) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_FORM_ACTIVE_PERIOD);
        }
        this.isAnonymous = isAnonymous;
        this.startsAt = startsAt;
        this.endsAtExclusive = endsAtExclusive;
    }

    public boolean isOpen(Instant now) {
        if (startsAt == null || endsAtExclusive == null) {
            return true; // 기간 미설정이면 열려있다고 간주
        }
        return !now.isBefore(startsAt) && now.isBefore(endsAtExclusive);
    }

    public FormOpenStatus getOpenStatus(Instant now) {
        if (startsAt != null && now.isBefore(startsAt)) {
            return FormOpenStatus.NOT_STARTED;
        }
        if (endsAtExclusive != null && !now.isBefore(endsAtExclusive)) {
            return FormOpenStatus.CLOSED;
        }
        return FormOpenStatus.OPEN;
    }

    public static Form createPublished(Long createdMemberId, String title) {
        Form form = new Form();
        form.createdMemberId = createdMemberId;
        form.title = title;
        form.status = FormStatus.PUBLISHED;
        return form;
    }

    /**
     * 투표는 섹션 1개 / 질문 1개를 생성하고, options 순서대로 orderNo=1..N 부여.
     */
    public void appendSingleQuestion(String questionText, QuestionType type, List<String> optionContents) {
        int sectionOrderNo = this.sections.size() + 1;

        FormSection section = FormSection.create(
            this,
            FormSectionType.DEFAULT,
            null,
            this.title,
            sectionOrderNo
        );
        this.sections.add(section);

        int questionOrderNo = 1;
        Question q = Question.create(questionText, type, true, questionOrderNo);
        section.addQuestion(q);

        int optionOrderNo = 1;
        for (String content : optionContents) {
            q.addOption(QuestionOption.create(content, optionOrderNo++, false));
        }
    }
}
