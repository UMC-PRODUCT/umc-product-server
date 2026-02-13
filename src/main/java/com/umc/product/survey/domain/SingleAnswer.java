package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
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
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "single_answer")
public class SingleAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private FormResponse formResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(name = "answered_as_type", nullable = false)
    private QuestionType answeredAsType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> value;

    public static SingleAnswer create(
        FormResponse formResponse,
        Question question,
        QuestionType answeredAsType,
        Map<String, Object> value
    ) {
        SingleAnswer a = new SingleAnswer();
        a.formResponse = formResponse;
        a.question = question;
        a.answeredAsType = answeredAsType;
        a.value = (value == null) ? Map.of() : value;
        return a;
    }

    public void change(QuestionType answeredAsType, Map<String, Object> value) {
        this.answeredAsType = answeredAsType;
        this.value = (value == null) ? Map.of() : value;
    }

    public static SingleAnswer createVoteAnswer(
        FormResponse formResponse,
        Question question,
        List<Long> selectedOptionIds
    ) {
        QuestionType t = question.getType();

        if (t == QuestionType.RADIO || t == QuestionType.DROPDOWN) {
            Long selectedOptionId = selectedOptionIds.get(0);
            return SingleAnswer.create(
                formResponse,
                question,
                t,
                Map.of("selectedOptionId", selectedOptionId)
            );
        }

        if (t == QuestionType.CHECKBOX) {
            return SingleAnswer.create(
                formResponse,
                question,
                t,
                Map.of("selectedOptionIds", selectedOptionIds)
            );
        }

        // 여기 도달 시 구조이상
        throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_QUESTION_TYPE);
    }
}

