package com.umc.product.github.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.github.application.port.in.dto.GithubWebhookCommand;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class GithubWebhookMetricServiceTest {

    private SimpleMeterRegistry registry;
    private GithubWebhookMetricService service;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        service = new GithubWebhookMetricService(new ObjectMapper(), new GithubMetrics(registry));
    }

    @Test
    @DisplayName("PR opened 이벤트는 repo/action 라벨로 PR 카운터를 증가시킨다")
    void PR_생성_카운트() {
        String payload = """
            {"action":"opened","repository":{"name":"umc-product-server"},"pull_request":{}}
            """;

        service.handle(new GithubWebhookCommand("pull_request", "d1", payload));

        var counter = registry.find("github.pull_request")
            .tag("repo", "umc-product-server")
            .tag("action", "opened")
            .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("PR merged 이벤트는 merged 카운트·time_to_close·크기 분포를 기록한다")
    void PR_머지_지표_기록() {
        String payload = """
            {"action":"closed","repository":{"name":"r"},
             "pull_request":{"merged":true,
               "created_at":"2026-06-20T10:00:00Z","merged_at":"2026-06-20T11:00:00Z",
               "additions":10,"deletions":5,"changed_files":3}}
            """;

        service.handle(new GithubWebhookCommand("pull_request", "d2", payload));

        assertThat(registry.find("github.pull_request").tag("action", "merged").counter().count())
            .isEqualTo(1.0);
        assertThat(registry.find("github.pr.time_to_close").tag("result", "merged").timer()
            .totalTime(TimeUnit.SECONDS)).isEqualTo(3600.0);
        assertThat(registry.find("github.pr.size_lines").tag("repo", "r").summary().totalAmount())
            .isEqualTo(15.0);
        assertThat(registry.find("github.pr.changed_files").tag("repo", "r").summary().totalAmount())
            .isEqualTo(3.0);
    }

    @Test
    @DisplayName("PR closed(merged=false) 이벤트는 closed 결과로 기록된다")
    void PR_단순종료_지표_기록() {
        String payload = """
            {"action":"closed","repository":{"name":"r"},
             "pull_request":{"merged":false,
               "created_at":"2026-06-20T10:00:00Z","closed_at":"2026-06-20T10:10:00Z",
               "additions":1,"deletions":1,"changed_files":1}}
            """;

        service.handle(new GithubWebhookCommand("pull_request", "d6", payload));

        assertThat(registry.find("github.pull_request").tag("action", "closed").counter().count())
            .isEqualTo(1.0);
        assertThat(registry.find("github.pr.time_to_close").tag("result", "closed").timer()
            .totalTime(TimeUnit.SECONDS)).isEqualTo(600.0);
    }

    @Test
    @DisplayName("Issue closed 이벤트는 카운트와 time_to_close 를 기록한다")
    void 이슈_종료_지표_기록() {
        String payload = """
            {"action":"closed","repository":{"name":"r"},
             "issue":{"created_at":"2026-06-20T10:00:00Z","closed_at":"2026-06-20T10:30:00Z"}}
            """;

        service.handle(new GithubWebhookCommand("issues", "d3", payload));

        assertThat(registry.find("github.issue").tag("action", "closed").counter().count())
            .isEqualTo(1.0);
        assertThat(registry.find("github.issue.time_to_close").tag("repo", "r").timer()
            .totalTime(TimeUnit.SECONDS)).isEqualTo(1800.0);
    }

    @Test
    @DisplayName("메트릭 대상이 아닌 이벤트는 어떤 메트릭도 만들지 않는다")
    void 무관한_이벤트_무시() {
        service.handle(new GithubWebhookCommand("push", "d4", "{}"));

        assertThat(registry.getMeters()).isEmpty();
    }

    @Test
    @DisplayName("깨진 JSON 페이로드는 예외 없이 무시된다")
    void 깨진_페이로드_무시() {
        service.handle(new GithubWebhookCommand("pull_request", "d5", "{not json"));

        assertThat(registry.getMeters()).isEmpty();
    }
}
