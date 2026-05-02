package com.umc.product.project.domain.enums;

/**
 * 지원 폼 섹션의 가시성 타입.
 * <p>
 * {@link #COMMON} 은 모든 파트 챌린저에게 노출되며 {@code allowed_parts} 가 무시된다.
 * {@link #PART} 는 {@code allowed_parts} 에 명시된 챌린저 파트만 해당 섹션을 볼 수 있다.
 */
public enum FormSectionType {
    COMMON,
    PART,
}
