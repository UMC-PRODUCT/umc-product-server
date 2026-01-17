package com.umc.product.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${app.firebase-configuration}")
    private String firebaseCredentials;

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        log.info("Firebase 초기화 시작");

        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(firebaseCredentials.getBytes(StandardCharsets.UTF_8))
        );
        log.debug("Firebase credentials 로드 완료");

        FirebaseApp firebaseApp = null;
        List<FirebaseApp> apps = FirebaseApp.getApps();

        if (apps != null && !apps.isEmpty()) {
            log.info("기존 FirebaseApp 검색 중... (총 {}개)", apps.size());
            for (FirebaseApp app : apps) {
                if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                    firebaseApp = app;
                    log.info("기존 FirebaseApp 발견: {}", app.getName());
                }
            }
        } else {
            log.info("FirebaseApp 신규 생성");
            FirebaseOptions options =
                    FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();
            firebaseApp = FirebaseApp.initializeApp(options);
            log.info("FirebaseApp 초기화 완료: {}", firebaseApp.getName());
        }

        log.info("FirebaseMessaging 인스턴스 생성 완료");
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
