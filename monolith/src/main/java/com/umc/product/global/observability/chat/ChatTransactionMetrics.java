package com.umc.product.global.observability.chat;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionExecution;
import org.springframework.transaction.TransactionExecutionListener;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

/** 커뮤니티 메시지 명령의 실제 트랜잭션을 관측한다. 중첩 REQUIRED 호출은 중복 집계하지 않는다. */
@Component
@RequiredArgsConstructor
public class ChatTransactionMetrics implements TransactionExecutionListener {

    private static final String SERVICE =
        "com.umc.product.community.application.service.message.CommunityThreadMessageCommandService.";
    private static final Duration[] BUCKETS = {Duration.ofMillis(1), Duration.ofMillis(5), Duration.ofMillis(10),
        Duration.ofMillis(25), Duration.ofMillis(50), Duration.ofMillis(100), Duration.ofMillis(250),
        Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(5),
        Duration.ofSeconds(10), Duration.ofSeconds(30)};

    private final MeterRegistry registry;
    private final ThreadLocal<Sample> current = new ThreadLocal<>();

    @Override
    public void beforeBegin(TransactionExecution transaction) {
        String operation = operation(transaction.getTransactionName());
        Sample previous = current.get();
        if (operation != null || previous != null) {
            // 다른 REQUIRES_NEW 트랜잭션의 SQL을 바깥 명령에 합산하지 않는다.
            current.set(new Sample(transaction, operation, now(), previous));
        }
    }

    @Override
    public void afterBegin(TransactionExecution transaction, Throwable failure) {
        if (failure != null) {
            finish(transaction, "begin_failed");
        }
    }

    @Override
    public void afterCommit(TransactionExecution transaction, Throwable failure) {
        finish(transaction, failure == null ? "committed" : "commit_failed");
    }

    @Override
    public void afterRollback(TransactionExecution transaction, Throwable failure) {
        finish(transaction, failure == null ? "rolled_back" : "rollback_failed");
    }

    public void recordSql(String sql, long elapsedNanos, SQLException failure, boolean batch) {
        Sample sample = current.get();
        if (sample == null || sample.operation == null || sample.completedAt != 0) {
            return;
        }
        sample.jdbcCalls++;
        ChatSqlKind kind = ChatSqlKind.from(sql);
        timer("umc.chat.sql", "operation", sample.operation, "table", kind.table(),
            "statement", kind.statement(), "execution", batch ? "batch" : "single",
            "outcome", ChatSqlKind.outcome(failure)).record(elapsedNanos, TimeUnit.NANOSECONDS);
        if (failure == null && kind.statement().equals("select_for_update") && sample.firstLockAt == 0) {
            sample.firstLockAt = now();
        }
    }

    public void beforeJdbcCommit() {
        Sample sample = current.get();
        if (sample != null) {
            sample.commitStartedAt = now();
        }
    }

    public void afterJdbcCompletion(SQLException failure) {
        Sample sample = current.get();
        if (sample != null && failure == null) {
            sample.completedAt = now();
        }
    }

    private void finish(TransactionExecution transaction, String outcome) {
        Sample sample = current.get();
        if (sample == null || sample.transaction != transaction) {
            return;
        }
        try {
            if (sample.operation != null) {
                recordCompletion(sample, outcome);
            }
        } finally {
            if (sample.previous == null) {
                current.remove();
            } else {
                current.set(sample.previous);
            }
        }
    }

    private void recordCompletion(Sample sample, String outcome) {
        long end = sample.completedAt == 0 ? now() : sample.completedAt;
        String boundary = sample.completedAt == 0 ? "spring" : "jdbc";
        recordPhase(sample, outcome, boundary, "total", end - sample.startedAt);
        if (sample.firstLockAt != 0) {
            recordPhase(sample, outcome, boundary, "after_first_lock", end - sample.firstLockAt);
        }
        if (sample.commitStartedAt != 0 && sample.completedAt != 0 && outcome.equals("committed")) {
            recordPhase(sample, outcome, boundary, "commit", end - sample.commitStartedAt);
        }
        DistributionSummary.builder("umc.chat.jdbc.calls")
            .description("명령 트랜잭션의 JDBC 실행 호출 수, 배치는 한 호출")
            .tags("operation", sample.operation, "outcome", outcome)
            .register(registry).record(sample.jdbcCalls);
    }

    private void recordPhase(Sample sample, String outcome, String boundary, String phase, long elapsed) {
        timer("umc.chat.transaction", "operation", sample.operation, "outcome", outcome,
            "boundary", boundary, "phase", phase).record(elapsed, TimeUnit.NANOSECONDS);
    }

    private Timer timer(String name, String... tags) {
        return Timer.builder(name).tags(tags).serviceLevelObjectives(BUCKETS).register(registry);
    }

    private long now() {
        return registry.config().clock().monotonicTime();
    }

    private String operation(String name) {
        if (name == null || !name.startsWith(SERVICE)) {
            return null;
        }
        return switch (name.substring(SERVICE.length())) {
            case "create" -> "message_create";
            case "edit" -> "message_edit";
            case "tombstone" -> "message_delete";
            case "add" -> "reaction_add";
            case "remove" -> "reaction_remove";
            case "update" -> "read_update";
            default -> null;
        };
    }

    private static final class Sample {
        private final TransactionExecution transaction;
        private final String operation;
        private final long startedAt;
        private final Sample previous;
        private long firstLockAt;
        private long commitStartedAt;
        private long completedAt;
        private int jdbcCalls;

        private Sample(TransactionExecution transaction, String operation, long startedAt, Sample previous) {
            this.transaction = transaction;
            this.operation = operation;
            this.startedAt = startedAt;
            this.previous = previous;
        }
    }
}
