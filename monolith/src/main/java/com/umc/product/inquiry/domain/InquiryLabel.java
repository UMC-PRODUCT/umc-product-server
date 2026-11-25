package com.umc.product.inquiry.domain;

import java.util.Objects;

import com.umc.product.common.BaseEntity;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inquiry_label")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryLabel extends BaseEntity {

    public static final int MAX_NAME_CODE_POINTS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_NAME_CODE_POINTS)
    private String name;

    public static InquiryLabel create(String name) {
        InquiryLabel label = new InquiryLabel();
        label.name = normalize(name);
        return label;
    }

    public void rename(String name) {
        this.name = normalize(name);
    }

    private static String normalize(String name) {
        String trimmed = Objects.requireNonNull(name).strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        if (length < 1 || length > MAX_NAME_CODE_POINTS) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_LABEL_NAME);
        }
        return trimmed;
    }
}
