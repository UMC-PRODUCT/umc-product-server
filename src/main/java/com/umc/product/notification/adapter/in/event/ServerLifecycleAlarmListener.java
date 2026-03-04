package com.umc.product.notification.adapter.in.event;

import com.umc.product.notification.application.port.in.SendWebhookAlarmUseCase;
import com.umc.product.notification.application.port.in.dto.SendWebhookAlarmCommand;
import com.umc.product.notification.domain.WebhookPlatform;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!local")
public class ServerLifecycleAlarmListener {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<WebhookPlatform> PLATFORMS = List.of(WebhookPlatform.TELEGRAM, WebhookPlatform.DISCORD);

    private final SendWebhookAlarmUseCase sendWebhookAlarmUseCase;
    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String profile = getCapitalizedProfile();
        String time = LocalDateTime.now().format(FORMATTER);

        SendWebhookAlarmCommand command = SendWebhookAlarmCommand.builder()
            .platforms(PLATFORMS)
            .title("[" + profile + "] 서버 시작")
            .content("프로필: " + profile + "\n시각: " + time)
            .build();

        sendWebhookAlarmUseCase.send(command);
        log.info("서버 시작 알림 전송 완료 [profile={}, time={}]", profile, time);
    }

    @PreDestroy
    public void onShutdown() {
        String profile = getCapitalizedProfile();
        String time = LocalDateTime.now().format(FORMATTER);

        SendWebhookAlarmCommand command = SendWebhookAlarmCommand.builder()
            .platforms(PLATFORMS)
            .title("[" + profile + "] 서버 종료")
            .content("프로필: " + profile + "\n시각: " + time)
            .build();

        sendWebhookAlarmUseCase.send(command);
        log.info("서버 종료 알림 전송 완료 [profile={}, time={}]", profile, time);
    }

    private String getCapitalizedProfile() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            return "Default";
        }
        return String.join(", ", java.util.Arrays.stream(profiles)
            .map(p -> p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase())
            .toList());
    }
}
