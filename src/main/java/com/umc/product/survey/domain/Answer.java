package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.survey.domain.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "answer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_response_id", nullable = false)
    private FormResponse formResponse; // 전체 설문 응답지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question; // 어떤 질문에 대한 답인가?

    // 주관식(SHORT_TEXT, LONG_TEXT)인 경우에만 값이 들어가고, 객관식이면 NULL
    @Column(name = "text_value", columnDefinition = "TEXT")
    private String textValue;

    /*
    주관식(SHORT_TEXT, LONG_TEXT)로 응답한 경우: answer.textValue
    객관식 단일 응답 (RADIO, DROPDOWN)으로 응답한 경우: answerChoice.questionOption (1개)
    객관식 다중 선택 (CHECKBOX)으로 응답한 경우: answerChoice.questionOption (N개)
    SCHEDULE 타입 질문: answer.times (N개)
    PORTFOLIO 타입 질문: textValue 또는 fileIds
    FILE 타입 질문: fileIds
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType answeredAsType;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "file_ids", columnDefinition = "bigint[]")
    private Set<Long> fileIds;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "times", columnDefinition = "timestampz[]")
    private Set<Instant> times;

    // 다중/단일 선택 객관식 답변을 위한 1:N 양방향 매핑
    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerChoice> choices = new ArrayList<>();

    public static Answer create(
        FormResponse formResponse,
        Question question,
        QuestionType answeredAsType,
        String textValue
    ) {
        Answer answer = new Answer();
        answer.formResponse = formResponse;
        answer.question = question;
        answer.answeredAsType = answeredAsType;
        answer.textValue = textValue;

        return answer;
    }

    // 객관식 보기 추가 편의 메서드
    public void addChoice(QuestionOption option) {
        this.choices.add(new AnswerChoice(this, option));
    }
}
