package com.umc.product.test.adapter.in.runner;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.umc.product.test.application.port.in.command.SeedQaDataUseCase;
import com.umc.product.test.application.port.in.command.dto.QaStepResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 앱이 뜬 뒤 QA 시딩 전체(`POST /test/seed/qa`와 같음)를 한 번 실행한다. app.seed.qa-auto-run=true 인 환경(dev)에서만 켠다.
 * <p>
 * 처음 한 번은 계정 593개의 비밀번호 해시 때문에 수십 초가 걸려 기동을 막지 않도록 비동기로 돈다. 단계마다 이미 있으면 건너뛰므로 재시작마다 돌아도
 * 결과가 같다.
 */
@Slf4j
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = {"enabled", "qa-auto-run"}, havingValue = "true")
@RequiredArgsConstructor
public class QaSeedAutoRunner {

    private final SeedQaDataUseCase seedQaDataUseCase;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void seedOnReady() {
        run();
    }

    List<QaStepResult> run() {
        try {
            List<QaStepResult> results = seedQaDataUseCase.seedAll();
            results.forEach(result -> log.info(
                "QA 시딩 자동 실행: step={}, status={}, created={}, skipped={}, failed={}, error={}",
                result.step(), result.status(), result.created(), result.skipped(), result.failed(),
                result.errorCode()));
            return results;
        } catch (RuntimeException e) {
            log.error("QA 시딩 자동 실행 실패", e);
            return List.of();
        }
    }
}
