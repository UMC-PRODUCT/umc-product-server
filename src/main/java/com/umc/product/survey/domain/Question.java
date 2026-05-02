package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.survey.domain.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
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
        String title,
        QuestionType type,
        boolean isRequired,
        long orderNo
    ) {
        return Question.builder()
            .title(title)
            .type(type)
            .isRequired(isRequired)
            .orderNo(orderNo)
            .build();
    }

    public void assignTo(FormSection section) {
        this.formSection = section;
    }

    /**
     * 질문 속성 부분 업데이트 (PATCH semantics).
     * null 인 필드는 기존 값 유지. type 변경은 별도 {@link #changeType} 사용.
     */
    public void update(String title, String description, Boolean isRequired) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (isRequired != null) {
            this.isRequired = isRequired;
        }
    }

    /**
     * 질문 타입 변경. 호출 측 Service 가 옵션 / 응답 cascade 정리 책임.
     */
    public void changeType(QuestionType type) {
        this.type = type;
    }

    public void updateOrderNo(long orderNo) {
        this.orderNo = orderNo;
    }

}
