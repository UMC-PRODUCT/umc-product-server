package com.umc.product.notification.adapter.out.external;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app.notification.email")
public record EmailProviderProperties(@DefaultValue("ses") @NotNull Provider provider) {

    public enum Provider {
        SES,
        SMTP,
        // 로컬 개발용. 메일을 보내지 않고 로그만 남긴다.
        LOG
    }
}
