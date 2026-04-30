package com.umc.product.project.adapter.in.web.dto.common;

import com.umc.product.survey.domain.enums.QuestionType;
import java.util.List;
import lombok.Builder;

/**
 * 지원 문항 한 개. DraftProjectResponse와 UpsertApplicationFormRequest 양쪽에서 재사용됩니다.
 */
@Builder
public record ApplicationQuestionItem(
    long order,
    QuestionType type,
    String title,
    boolean required,
    List<String> options
) { }
