package com.umc.product.authentication.application.event;

/**
 * 이메일 인증 세션 생성/재발급 후, 메일 발송이 필요할 때 트랜잭션 commit 직후
 * 비동기로 메일을 보내기 위해 발행되는 애플리케이션 이벤트.
 * <p>
 * SMTP 호출이 트랜잭션 내부에서 일어나면 메일 서버 응답이 느릴 때 DB 커넥션을 길게
 * 점유하고, 발송 실패가 세션 롤백을 유발해 사용자가 처음부터 다시 인증해야 하는
 * 문제가 있었다. AFTER_COMMIT 단계로 분리해 양쪽 부작용을 끊는다.
 */
public record SendVerificationEmailEvent(
    String email,
    String verificationCode
) {
}
