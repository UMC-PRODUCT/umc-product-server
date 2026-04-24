package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "answer_choice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerChoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    // 길이 변경 시 QuestionOption.content도 함께 변경
    @Column(name = "answered_as_content", nullable = false, length = 500)
    private String answeredAsContent;

    // 사용자가 선택한 '보기'의 ID를 참조
    // TODO: repository에 JPQL 만들기
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_option_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private QuestionOption questionOption;

    public AnswerChoice(Answer answer, QuestionOption questionOption) {
        this.answer = answer;
        this.questionOption = questionOption;
        this.answeredAsContent = questionOption.getContent();
    }
}
