package com.umc.product.notification.application.port.in;

import com.umc.product.notification.application.port.in.dto.SendWebhookAlarmCommand;

public interface SendWebhookAlarmUseCase {

    void send(SendWebhookAlarmCommand command);

    void sendBuffered(SendWebhookAlarmCommand command);
}
