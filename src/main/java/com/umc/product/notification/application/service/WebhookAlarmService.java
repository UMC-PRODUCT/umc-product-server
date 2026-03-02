package com.umc.product.notification.application.service;

import com.umc.product.notification.application.port.in.FlushWebhookBufferUseCase;
import com.umc.product.notification.application.port.in.SendWebhookAlarmUseCase;
import com.umc.product.notification.application.port.in.dto.SendWebhookAlarmCommand;
import com.umc.product.notification.application.port.out.SendWebhookPort;
import com.umc.product.notification.domain.WebhookPlatform;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebhookAlarmService implements SendWebhookAlarmUseCase, FlushWebhookBufferUseCase {

    private final Map<WebhookPlatform, SendWebhookPort> adapterMap;
    private final WebhookAlarmBuffer webhookAlarmBuffer;

    public WebhookAlarmService(List<SendWebhookPort> adapters, WebhookAlarmBuffer webhookAlarmBuffer) {
        this.adapterMap = adapters.stream()
            .collect(Collectors.toMap(SendWebhookPort::platform, Function.identity()));
        this.webhookAlarmBuffer = webhookAlarmBuffer;
    }

    @Override
    public void send(SendWebhookAlarmCommand command) {
        for (WebhookPlatform platform : command.platforms()) {
            trySend(platform, command.title(), command.content());
        }
    }

    @Override
    public void sendBuffered(SendWebhookAlarmCommand command) {
        webhookAlarmBuffer.add(command);
        log.debug("웹훅 알람 버퍼에 추가: title={}, platforms={}", command.title(), command.platforms());
    }

    @Override
    public void flush() {
        List<SendWebhookAlarmCommand> commands = webhookAlarmBuffer.drainAll();
        if (commands.isEmpty()) {
            return;
        }

        log.info("버퍼된 웹훅 알람 전송 시작: count={}", commands.size());

        Map<WebhookPlatform, List<SendWebhookAlarmCommand>> grouped = groupByPlatform(commands);

        for (var entry : grouped.entrySet()) {
            WebhookPlatform platform = entry.getKey();
            List<SendWebhookAlarmCommand> platformCommands = entry.getValue();
            String mergedContent = mergeMessages(platformCommands);
            String title = "알림 모아보기 (" + platformCommands.size() + "건)";

            trySend(platform, title, mergedContent);
        }
    }

    private void trySend(WebhookPlatform platform, String title, String content) {
        SendWebhookPort adapter = adapterMap.get(platform);
        if (adapter == null) {
            log.warn("웹훅 어댑터가 등록되지 않았습니다: platform={}", platform);
            return;
        }

        try {
            adapter.send(title, content);
            log.info("웹훅 알람 전송 성공: platform={}", platform);
        } catch (Exception e) {
            log.error("웹훅 알람 전송 실패: platform={}, error={}", platform, e.getMessage(), e);
        }
    }

    private Map<WebhookPlatform, List<SendWebhookAlarmCommand>> groupByPlatform(
        List<SendWebhookAlarmCommand> commands
    ) {
        Map<WebhookPlatform, List<SendWebhookAlarmCommand>> grouped = new EnumMap<>(WebhookPlatform.class);
        for (SendWebhookAlarmCommand command : commands) {
            for (WebhookPlatform platform : command.platforms()) {
                grouped.computeIfAbsent(platform, k -> new ArrayList<>()).add(command);
            }
        }
        return grouped;
    }

    private String mergeMessages(List<SendWebhookAlarmCommand> commands) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commands.size(); i++) {
            SendWebhookAlarmCommand cmd = commands.get(i);
            if (i > 0) {
                sb.append("\n\n---\n\n");
            }
            sb.append("[").append(cmd.title()).append("]\n");
            sb.append(cmd.content());
        }
        return sb.toString();
    }
}
