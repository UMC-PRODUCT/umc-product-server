package com.umc.product.github.application.service;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.github.application.port.in.HandleGithubWebhookUseCase;
import com.umc.product.github.application.port.in.dto.GithubWebhookCommand;

import lombok.RequiredArgsConstructor;

/**
 * GitHub 웹훅 페이로드를 파싱해 {@link GithubMetrics} 로 집계하는 서비스 (ADR-010 Phase A).
 *
 * <p>관심 이벤트는 {@code pull_request} 와 {@code issues} 두 가지뿐이며, 그 외 이벤트는 무시한다.
 * 모든 소요 시간(time to close)은 종료 이벤트 페이로드에 함께 들어오는 created/closed/merged 타임스탬프로
 * 그 자리에서 계산하므로 별도 저장소가 필요 없다 (무상태).
 *
 * <p>{@code @Async} 로 동작해 컨트롤러는 서명 검증 직후 200 을 즉시 반환한다. 파싱 실패 등 예외는
 * 외부 입력에 기인하므로 {@code warn} 으로만 남기고 삼킨다 (메트릭 한 건 누락이 서비스에 영향을 주지 않음).
 */
@Service
@RequiredArgsConstructor
public class GithubWebhookMetricService implements HandleGithubWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(GithubWebhookMetricService.class);

    private static final String EVENT_PULL_REQUEST = "pull_request";
    private static final String EVENT_ISSUES = "issues";
    private static final String UNKNOWN_REPO = "unknown";

    private final ObjectMapper objectMapper;
    private final GithubMetrics githubMetrics;

    @Async
    @Override
    public void handle(GithubWebhookCommand command) {
        try {
            JsonNode root = objectMapper.readTree(command.payload());
            switch (command.eventType()) {
                case EVENT_PULL_REQUEST -> handlePullRequest(root);
                case EVENT_ISSUES -> handleIssues(root);
                case null, default -> {
                    // 구독했지만 메트릭 대상이 아닌 이벤트(또는 헤더 누락으로 eventType 이 null)는 조용히 무시한다.
                }
            }
        } catch (Exception e) {
            log.warn("GitHub 웹훅 처리 실패: event={}, delivery={}, error={}",
                command.eventType(), command.deliveryId(), e.getClass().getSimpleName());
        }
    }

    private void handlePullRequest(JsonNode root) {
        String action = root.path("action").asText("");
        String repo = root.path("repository").path("name").asText(UNKNOWN_REPO);
        JsonNode pr = root.path("pull_request");

        switch (action) {
            case "opened", "reopened" -> githubMetrics.countPullRequest(repo, action);
            case "closed" -> {
                // PR merged 여부는 별도 이벤트가 아니라 closed + pull_request.merged=true 조합으로만 판별된다.
                boolean merged = pr.path("merged").asBoolean(false);
                String result = merged ? "merged" : "closed";
                githubMetrics.countPullRequest(repo, result);

                Instant created = parseInstant(pr.path("created_at"));
                Instant closed = parseInstant(merged ? pr.path("merged_at") : pr.path("closed_at"));
                if (isValidInterval(created, closed)) {
                    githubMetrics.recordPullRequestTimeToClose(repo, result, Duration.between(created, closed));
                }

                int additions = pr.path("additions").asInt(0);
                int deletions = pr.path("deletions").asInt(0);
                int changedFiles = pr.path("changed_files").asInt(0);
                githubMetrics.recordPullRequestSize(repo, additions + deletions, changedFiles);
            }
            default -> {
                // edited/synchronize/labeled 등은 메트릭 대상이 아니다.
            }
        }
    }

    private void handleIssues(JsonNode root) {
        String action = root.path("action").asText("");
        String repo = root.path("repository").path("name").asText(UNKNOWN_REPO);
        JsonNode issue = root.path("issue");

        switch (action) {
            case "opened", "reopened" -> githubMetrics.countIssue(repo, action);
            case "closed" -> {
                githubMetrics.countIssue(repo, "closed");
                Instant created = parseInstant(issue.path("created_at"));
                Instant closed = parseInstant(issue.path("closed_at"));
                if (isValidInterval(created, closed)) {
                    githubMetrics.recordIssueTimeToClose(repo, Duration.between(created, closed));
                }
            }
            default -> {
                // 그 외 action 은 무시.
            }
        }
    }

    private boolean isValidInterval(Instant start, Instant end) {
        return start != null && end != null && !end.isBefore(start);
    }

    private Instant parseInstant(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        try {
            return Instant.parse(node.asText());
        } catch (RuntimeException e) {
            return null;
        }
    }
}
