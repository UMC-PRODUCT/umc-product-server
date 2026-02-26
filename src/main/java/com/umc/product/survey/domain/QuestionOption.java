package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Integer orderNo; // 보기 순서

    @Column(name = "is_other", nullable = false)
    private boolean isOther;


    public void changeContent(String content) {
        this.content = content;
    }

    public void changeOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public void changeIsOther(boolean isOther) {
        this.isOther = isOther;
    }

    public static QuestionOption create(String content, int orderNo, boolean isOther) {
        QuestionOption questionOption = new QuestionOption();
        questionOption.content = content;
        questionOption.orderNo = orderNo;
        questionOption.isOther = isOther;
        return questionOption;
    }

    public void assignTo(Question question) {
        this.question = question;
    }

}
