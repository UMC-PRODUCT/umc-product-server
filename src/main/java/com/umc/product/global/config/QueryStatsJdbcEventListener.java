package com.umc.product.global.config;

import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * P6Spy JdbcEventListener를 통해 요청 단위 쿼리 통계를 수집합니다. p6spy-spring-boot-starter가 @Component JdbcEventListener 빈을 자동 등록합니다.
 */
@Component
@Slf4j
public class QueryStatsJdbcEventListener extends JdbcEventListener {

    @Override
    public void onAfterExecuteQuery(
        PreparedStatementInformation info, long timeElapsedNanos, SQLException e
    ) {
        log.debug(
            "[executeQuery] sql={}, elapsed={} ms",
            info.getSqlWithValues(), timeElapsedNanos / 1_000_000.0
        );

        record(timeElapsedNanos, e);
    }

    @Override
    public void onAfterExecuteUpdate(
        PreparedStatementInformation info, long timeElapsedNanos, int rowCount,
        SQLException e
    ) {
        log.debug(
            "[executeUpdate] sql={}, elapsed={} ms",
            info.getSqlWithValues(), timeElapsedNanos / 1_000_000.0
        );

        record(timeElapsedNanos, e);
    }

    @Override
    public void onAfterExecuteBatch(
        StatementInformation info, long timeElapsedNanos, int[] updateCounts,
        SQLException e
    ) {
        log.debug(
            "[executeBatch] sql={}, batchSize={}, elapsed={} ms",
            info.getSqlWithValues(), updateCounts != null ? updateCounts.length : 0, timeElapsedNanos / 1_000_000.0
        );

        record(timeElapsedNanos, e);
    }


    private void record(long timeElapsedNanos, SQLException e) {
        if (e == null) {
            QueryStatsHolder.record(timeElapsedNanos);
        }
    }
}
