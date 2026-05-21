package com.umc.product.notification.application.port.out.dto;

/**
 * 이메일 발송 어댑터로 전달되는 메시지 단위.
 *
 * <p>application 레이어에서 템플릿 렌더링까지 완료한 결과를 담는다. 발신자(from), Configuration Set 등
 * 인프라 종속 설정은 adapter 레이어에서 주입한다.
 */
public record EmailMessage(
    String to,
    String subject,
    String htmlBody
) {
}
