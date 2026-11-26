package com.umc.product.inquiry.domain;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryFaqContent {

    public static final int MAX_QUESTION_CODE_POINTS = 200;
    public static final int MAX_ANSWER_CODE_POINTS = 5_000;
    public static final int MAX_ALIAS_CODE_POINTS = 50;
    public static final int MAX_ALIASES = 20;

    @Column(name = "question")
    private String question;

    @Column(name = "answer")
    private String answer;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "aliases", columnDefinition = "text[]")
    private List<String> aliases;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "audience_id")
    private Long audienceId;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "related_faq_id")
    private Long relatedFaqId;

    public static InquiryFaqContent of(
        String question,
        String answer,
        List<String> aliases,
        Long categoryId,
        Long audienceId,
        LocalDate endDate,
        Long relatedFaqId
    ) {
        InquiryFaqContent content = new InquiryFaqContent();
        content.question = normalizeQuestion(question);
        content.answer = normalizeAnswer(answer);
        content.aliases = normalizeAliases(aliases);
        content.categoryId = categoryId;
        content.audienceId = audienceId;
        content.endDate = endDate;
        content.relatedFaqId = relatedFaqId;
        return content;
    }

    public static InquiryFaqContent draftOf(String question, Long categoryId) {
        return of(question, null, List.of(), categoryId, null, null, null);
    }

    boolean isPublishable() {
        return answer != null && categoryId != null && audienceId != null;
    }

    boolean isVisibleOn(LocalDate today) {
        return endDate == null || !today.isAfter(endDate);
    }

    String searchText() {
        return Stream.concat(Stream.concat(Stream.of(question), aliases.stream()), Stream.of(answer))
            .filter(Objects::nonNull)
            .collect(Collectors.joining("\n"));
    }

    private static String normalizeQuestion(String question) {
        String trimmed = Objects.requireNonNull(question).strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        if (length < 1 || length > MAX_QUESTION_CODE_POINTS) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_FAQ_QUESTION);
        }
        return trimmed;
    }

    private static String normalizeAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            return null;
        }
        String trimmed = answer.strip();
        if (trimmed.codePointCount(0, trimmed.length()) > MAX_ANSWER_CODE_POINTS) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_FAQ_ANSWER);
        }
        return trimmed;
    }

    private static List<String> normalizeAliases(List<String> aliases) {
        if (aliases == null) {
            return List.of();
        }
        LinkedHashSet<String> distinct = aliases.stream()
            .filter(Objects::nonNull)
            .map(String::strip)
            .filter(alias -> !alias.isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        boolean tooLong = distinct.stream()
            .anyMatch(alias -> alias.codePointCount(0, alias.length()) > MAX_ALIAS_CODE_POINTS);
        if (tooLong || distinct.size() > MAX_ALIASES) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_FAQ_ALIASES);
        }
        return List.copyOf(distinct);
    }
}
