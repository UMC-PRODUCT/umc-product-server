package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.survey.domain.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "question")
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_section_id", nullable = false)
    private FormSection formSection;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    @Column(name = "order_no", nullable = false)
    private Long orderNo;

    public static Question create(
        String questionText,
        QuestionType type,
        boolean isRequired,
        long orderNo
    ) {
        Question q = new Question();
        q.title = questionText;
        q.type = type;
        q.isRequired = isRequired;
        q.orderNo = orderNo;
        return q;
    }

    public void assignTo(FormSection section) {
        this.formSection = section;
    }

    public void changeFormSection(FormSection section) {
        this.formSection = section;
    }

    public void changeQuestionText(String text) {
        this.title = text;
    }

    public void changeType(QuestionType type) {
        this.type = type;
    }

    public void changeRequired(boolean required) {
        this.isRequired = required;
    }

    public void changeOrderNo(long orderNo) {
        this.orderNo = orderNo;
    }

}
