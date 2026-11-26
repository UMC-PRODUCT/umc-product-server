package com.umc.product.inquiry.application.port.in.command.dto;

import java.time.LocalDate;
import java.util.List;

import com.umc.product.inquiry.domain.InquiryFaqContent;

/**
 * 대표 질문 수정본 전체. 초안 저장은 이 값으로 수정본을 통째로 바꾼다.
 *
 * @param endDate 이 날(한국 시간)까지 공개하고 다음 날부터 자동으로 내린다. null이면 계속 공개한다.
 */
public record InquiryFaqContentCommand(
    String question,
    String answer,
    List<String> aliases,
    Long categoryId,
    Long audienceId,
    LocalDate endDate,
    Long relatedFaqId
) {
    public InquiryFaqContent toContent() {
        return InquiryFaqContent.of(question, answer, aliases, categoryId, audienceId, endDate, relatedFaqId);
    }
}
