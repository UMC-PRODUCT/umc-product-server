package com.umc.product.inquiry.domain;

import java.util.Objects;

import com.umc.product.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "inquiry_label_assignment",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_inquiry_label_assignment", columnNames = {"inquiry_id", "label_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryLabelAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inquiry_id", nullable = false)
    private Long inquiryId;

    @Column(name = "label_id", nullable = false)
    private Long labelId;

    public static InquiryLabelAssignment create(Long inquiryId, Long labelId) {
        InquiryLabelAssignment assignment = new InquiryLabelAssignment();
        assignment.inquiryId = Objects.requireNonNull(inquiryId);
        assignment.labelId = Objects.requireNonNull(labelId);
        return assignment;
    }
}
