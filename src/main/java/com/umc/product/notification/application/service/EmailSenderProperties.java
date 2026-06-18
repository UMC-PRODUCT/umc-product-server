package com.umc.product.notification.application.service;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 이메일 발신자(From) 식별자 설정.
 *
 * <p>SES 등 인프라 종속이 아닌 비즈니스 식별자이므로 application 레이어에 둔다.
 * 발송 case 별로 발신자를 분리하기 위한 진입점이며, 향후 support 등 추가 case 가 생기면 필드를 확장한다.
 */
@Validated
@ConfigurationProperties(prefix = "app.notification.email.sender")
public record EmailSenderProperties(
    @NotBlank String noReplyAddress,
    @NotBlank String noReplyDisplayName
) {
}
