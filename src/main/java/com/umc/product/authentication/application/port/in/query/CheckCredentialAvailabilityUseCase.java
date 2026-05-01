package com.umc.product.authentication.application.port.in.query;

/**
 * 회원가입 / 자격증명 등록 화면에서 로그인 ID 사용 가능 여부를 확인하는 UseCase.
 */
public interface CheckCredentialAvailabilityUseCase {

    /**
     * 주어진 loginId 가 사용 가능한지 (=중복되지 않았는지) 반환한다.
     * <p>
     * 형식이 잘못된 경우는 false 가 아니라 {@code AuthenticationErrorCode.INVALID_LOGIN_ID_FORMAT} 예외로 응답한다.
     */
    boolean isLoginIdAvailable(String loginId);
}
