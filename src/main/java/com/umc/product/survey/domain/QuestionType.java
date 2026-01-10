package com.umc.product.survey.domain;

public enum QuestionType {
    SHORT_TEXT,  // 단답형
    LONG_TEXT,   // 장문형
    RADIO,       // 객관식 (단일 선택)
    CHECKBOX,    // 객관식 (다중 선택)
    DROPDOWN,     // 드롭다운
    SCHEDULE,
    PORTFOLIO
}
