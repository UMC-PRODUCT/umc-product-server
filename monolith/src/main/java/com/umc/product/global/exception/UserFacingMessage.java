package com.umc.product.global.exception;

/**
 * 앱은 오류 응답의 {@code message}만 화면에 띄운다. 이 표시를 단 {@link BusinessException}은 오류 코드의 고정 문구 대신
 * 예외 문구를 {@code message}로 내보낸다.
 */
public interface UserFacingMessage {
}
