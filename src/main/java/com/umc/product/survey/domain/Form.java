package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.survey.domain.enums.FormOpenStatus;
import com.umc.product.survey.domain.enums.FormSectionType;
import com.umc.product.survey.domain.enums.FormStatus;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyDomainException;
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

    @Deprecated
    // TODO: NoticeVote 쪽 도메인 메소드로
    public void setVotePolicy(boolean isAnonymous, Instant startsAt, Instant endsAtExclusive) {
        return ;
    }

    // TODO: NoticeVote 쪽 도메인 메소드로
    public FormOpenStatus getOpenStatus(Instant now) {
        return null;
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
