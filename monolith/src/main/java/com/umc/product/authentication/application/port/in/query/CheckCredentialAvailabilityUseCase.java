package com.umc.product.authentication.application.port.in.query;

/**
 * 회원가입 / 자격증명 등록 화면에서 이메일 사용 가능 여부를 확인하는 UseCase. ADR-017 흐름.
 */
public interface CheckCredentialAvailabilityUseCase {

    /**
     * 주어진 email 이 사용 가능한지 (=중복되지 않았는지) 반환한다.
     * <p>
     * 형식이 잘못된 경우는 false 가 아니라 {@code AuthenticationErrorCode.INVALID_EMAIL_FORMAT} 예외로 응답한다.
     */
    boolean isEmailAvailable(String email);
}
