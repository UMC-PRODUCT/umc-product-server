package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "question_option")
public class QuestionOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(nullable = false)
    private String content; // 보기 내용 (예: '남자', '여자')

    @Column(name = "order_no", nullable = false)
    private Long orderNo; // 보기 순서

    @Column(name = "is_other", nullable = false)
    private boolean isOther;

    public static QuestionOption create(String content, long orderNo, boolean isOther) {
        QuestionOption questionOption = new QuestionOption();
        questionOption.content = content;
        questionOption.orderNo = orderNo;
        questionOption.isOther = isOther;
        return questionOption;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeOrderNo(long orderNo) {
        this.orderNo = orderNo;
    }

    public void changeIsOther(boolean isOther) {
        this.isOther = isOther;
    }

    public void assignTo(Question question) {
        this.question = question;
    }

}
