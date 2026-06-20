package com.umc.product.github.application.service;

import java.time.Duration;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * GitHub 활동 메트릭 등록자 (ADR-010 Phase A — 무상태 메트릭).
 *
 * <p>cardinality 폭증을 막기 위해 라벨은 {@code repo} / {@code action} / {@code result} 의
 * bounded 한 값만 사용한다. PR 번호 · 작성자 · 리뷰어 login 등 무한 증가하는 값은 절대 라벨로 쓰지 않는다.
 *
 * <ul>
 *   <li>{@code github_pull_request_total{repo, action}} — PR opened/reopened/closed/merged 카운트</li>
 *   <li>{@code github_issue_total{repo, action}} — Issue opened/reopened/closed 카운트</li>
 *   <li>{@code github_pr_time_to_close_seconds{repo, result}} — PR 생성→종료 소요 (히스토그램)</li>
 *   <li>{@code github_issue_time_to_close_seconds{repo}} — Issue 생성→종료 소요 (히스토그램)</li>
 *   <li>{@code github_pr_size_lines{repo}} — PR 변경 라인 수 분포 (additions+deletions)</li>
 *   <li>{@code github_pr_changed_files{repo}} — PR 변경 파일 수 분포</li>
 * </ul>
 */
@Component
public class GithubMetrics {

    private static final String TAG_REPO = "repo";
    private static final String TAG_ACTION = "action";
    private static final String TAG_RESULT = "result";

    // Counter 는 Prometheus 에서 _total 접미사가 자동으로 붙으므로 메트릭 id 에 total 을 넣지 않는다.
    private static final String PR_COUNT = "github.pull_request";
    private static final String ISSUE_COUNT = "github.issue";
    private static final String PR_TIME_TO_CLOSE = "github.pr.time_to_close";
    private static final String ISSUE_TIME_TO_CLOSE = "github.issue.time_to_close";
    private static final String PR_SIZE_LINES = "github.pr.size_lines";
    private static final String PR_CHANGED_FILES = "github.pr.changed_files";

    private final MeterRegistry registry;

    public GithubMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void countPullRequest(String repo, String action) {
        Counter.builder(PR_COUNT)
            .tag(TAG_REPO, repo)
            .tag(TAG_ACTION, action)
            .register(registry)
            .increment();
    }

    public void countIssue(String repo, String action) {
        Counter.builder(ISSUE_COUNT)
            .tag(TAG_REPO, repo)
            .tag(TAG_ACTION, action)
            .register(registry)
            .increment();
    }

    public void recordPullRequestTimeToClose(String repo, String result, Duration duration) {
        Timer.builder(PR_TIME_TO_CLOSE)
            .tag(TAG_REPO, repo)
            .tag(TAG_RESULT, result)
            .publishPercentileHistogram()
            .register(registry)
            .record(duration);
    }

    public void recordIssueTimeToClose(String repo, Duration duration) {
        Timer.builder(ISSUE_TIME_TO_CLOSE)
            .tag(TAG_REPO, repo)
            .publishPercentileHistogram()
            .register(registry)
            .record(duration);
    }

    public void recordPullRequestSize(String repo, int changedLines, int changedFiles) {
        DistributionSummary.builder(PR_SIZE_LINES)
            .tag(TAG_REPO, repo)
            .publishPercentileHistogram()
            .register(registry)
            .record(changedLines);
        DistributionSummary.builder(PR_CHANGED_FILES)
            .tag(TAG_REPO, repo)
            .publishPercentileHistogram()
            .register(registry)
            .record(changedFiles);
    }
}
