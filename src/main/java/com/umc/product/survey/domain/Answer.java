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
import java.util.Set;

/**
 * 한 사용자가 한 질문에 대해 작성한 답변의 루트 엔티티.
 * <p>
 * 질문 타입에 따라 값이 어디에 저장되는지가 다르다:
 * <ul>
 *   <li>SHORT_TEXT / LONG_TEXT -> {@link #textValue}</li>
 *   <li>RADIO / DROPDOWN / CHECKBOX -> {@link AnswerChoice} (1 또는 N건)</li>
 *   <li>SCHEDULE → {@link #times}</li>
 *   <li>FILE → {@link #fileIds}</li>
 *   <li>PORTFOLIO → {@link #textValue} 또는 {@link #fileIds}</li>
 * </ul>
 * 객관식 보기는 {@link AnswerChoice} 가 따로 들고 있다.
 */
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
    @Column(name = "times", columnDefinition = "timestamptz[]")
    private Set<Instant> times;

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

    /**
     * 답변 내용(textValue) 갱신. 객관식 답변의 AnswerChoice 갈아끼움은 Service 책임.
     * <p>
     * 질문 type 변경은 {@code ManageQuestionUseCase}가 다루므로 본 메서드에서 type 변경 안 함.
     */
    public void updateTextValue(String textValue) {
        this.textValue = textValue;
    }
}
