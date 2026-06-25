package com.umc.product.notification.application.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.umc.product.global.logging.OperationalMetrics;
import com.umc.product.notification.application.port.in.FlushWebhookBufferUseCase;
import com.umc.product.notification.application.port.in.SendWebhookAlarmUseCase;
import com.umc.product.notification.application.port.in.dto.SendWebhookAlarmCommand;
import com.umc.product.notification.application.port.out.SendWebhookPort;
import com.umc.product.notification.domain.WebhookPlatform;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebhookAlarmService implements SendWebhookAlarmUseCase, FlushWebhookBufferUseCase {

    private final Map<WebhookPlatform, SendWebhookPort> adapterMap;
    private final WebhookAlarmBuffer webhookAlarmBuffer;
    private final String profilePrefix;
    private final OperationalMetrics operationalMetrics;

    public WebhookAlarmService(List<SendWebhookPort> adapters, WebhookAlarmBuffer webhookAlarmBuffer,
                               Environment environment, OperationalMetrics operationalMetrics) {
        this.adapterMap = adapters.stream()
            .collect(Collectors.toMap(SendWebhookPort::platform, Function.identity()));
        this.webhookAlarmBuffer = webhookAlarmBuffer;
        this.profilePrefix = buildProfilePrefix(environment);
        this.operationalMetrics = operationalMetrics;
    }

    @Override
    public void send(SendWebhookAlarmCommand command) {
        String prefixedTitle = profilePrefix + command.title();
        for (WebhookPlatform platform : command.platforms()) {
            trySend(platform, prefixedTitle, command.content());
        }
    }

    @Override
    public void sendBuffered(SendWebhookAlarmCommand command) {
        webhookAlarmBuffer.add(command);
        log.debug("웹훅 알람 버퍼에 추가: platforms={}, contentLength={}",
            command.platforms(), command.content() == null ? 0 : command.content().length());
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
            String title = profilePrefix + "알림 모아보기 (" + platformCommands.size() + "건)";

            trySend(platform, title, mergedContent);
        }
    }

    private void trySend(WebhookPlatform platform, String title, String content) {
        SendWebhookPort adapter = adapterMap.get(platform);
        if (adapter == null) {
            log.warn("웹훅 어댑터가 등록되지 않았습니다: platform={}", platform);
            operationalMetrics.recordNotification(platform.name(), "SEND_WEBHOOK", "missing_adapter", 1);
            return;
        }

        try {
            adapter.send(title, content);
            operationalMetrics.recordNotification(platform.name(), "SEND_WEBHOOK", "success", 1);
            log.info("웹훅 알람을 전송했습니다: platform={}", platform);
        } catch (Exception e) {
            operationalMetrics.recordNotification(platform.name(), "SEND_WEBHOOK", "failure", 1);
            log.warn("웹훅 알람 전송 실패: platform={}, error={}", platform, e.getMessage(), e);
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

    private static String buildProfilePrefix(Environment environment) {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            return "[Default] ";
        }
        String capitalized = Arrays.stream(profiles)
            .map(p -> p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase())
            .collect(Collectors.joining(", "));
        return "[" + capitalized + "] ";
    }
}
