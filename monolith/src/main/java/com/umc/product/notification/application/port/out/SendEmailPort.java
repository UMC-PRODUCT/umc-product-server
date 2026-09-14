package com.umc.product.notification.application.port.out;

import com.umc.product.notification.application.port.out.dto.EmailMessage;

/**
 * 이메일 발송 outbound port.
 *
 * <p>application 레이어가 인프라(AWS SES 등) 종류와 무관하게 발송을 위임할 수 있도록 추상화한다.
 * 구현체는 adapter/out 에 위치한다.
 */
public interface SendEmailPort {

    void send(EmailMessage message);
}
