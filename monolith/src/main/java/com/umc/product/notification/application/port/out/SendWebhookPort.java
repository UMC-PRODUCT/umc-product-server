package com.umc.product.notification.application.port.out;

import com.umc.product.notification.domain.WebhookPlatform;

public interface SendWebhookPort {

    void send(String title, String content);

    WebhookPlatform platform();
}
