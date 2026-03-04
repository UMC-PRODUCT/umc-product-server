package com.umc.product.notification.application.service;

import com.umc.product.notification.application.port.in.dto.SendWebhookAlarmCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Component;

@Component
public class WebhookAlarmBuffer {

    private final ConcurrentLinkedQueue<SendWebhookAlarmCommand> queue = new ConcurrentLinkedQueue<>();

    public void add(SendWebhookAlarmCommand command) {
        queue.add(command);
    }

    public List<SendWebhookAlarmCommand> drainAll() {
        List<SendWebhookAlarmCommand> drained = new ArrayList<>();
        SendWebhookAlarmCommand command;

        while ((command = queue.poll()) != null) {
            drained.add(command);
        }

        return drained;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
