package com.umc.product.recruitment.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.recruitment.domain.enums.PartKey;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "interview_question_sheet")
public class InterviewQuestionSheet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruitment_id", nullable = false)
    private Recruitment recruitment;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_key", nullable = false, length = 30)
    private PartKey partKey;

    @Column(name = "order_no", nullable = false)
    private Integer orderNo;

    @Column(name = "content", nullable = false, length = 300)
    private String content;

    @Builder
    public InterviewQuestionSheet(Recruitment recruitment, PartKey partKey, Integer orderNo, String content) {
        this.recruitment = recruitment;
        this.partKey = partKey;
        this.orderNo = orderNo;
        this.content = content;
    }

    private static String normalize(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isBlank() ? null : t;
    }

    public void changeContent(String content) {
        this.content = normalize(content);
    }

    public void changeOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }
}
