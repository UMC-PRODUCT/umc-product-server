package com.umc.product.global.observability.chat;

import java.sql.SQLException;

import org.springframework.stereotype.Component;

import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;

import lombok.RequiredArgsConstructor;

/** 바인딩 값이나 SQL 원문을 저장하지 않고 실행 결과만 전달한다. */
@Component
@RequiredArgsConstructor
public class ChatMetricsJdbcEventListener extends JdbcEventListener {

    private final ChatTransactionMetrics metrics;

    @Override
    public void onAfterExecute(PreparedStatementInformation info, long elapsed, SQLException failure) {
        metrics.recordSql(info.getSql(), elapsed, failure, false);
    }

    @Override
    public void onAfterExecuteQuery(PreparedStatementInformation info, long elapsed, SQLException failure) {
        metrics.recordSql(info.getSql(), elapsed, failure, false);
    }

    @Override
    public void onAfterExecuteUpdate(PreparedStatementInformation info, long elapsed, int rows, SQLException failure) {
        metrics.recordSql(info.getSql(), elapsed, failure, false);
    }

    @Override
    public void onAfterExecute(StatementInformation info, long elapsed, String sql, SQLException failure) {
        metrics.recordSql(sql, elapsed, failure, false);
    }

    @Override
    public void onAfterExecuteQuery(StatementInformation info, long elapsed, String sql, SQLException failure) {
        metrics.recordSql(sql, elapsed, failure, false);
    }

    @Override
    public void onAfterExecuteUpdate(StatementInformation info, long elapsed, String sql, int rows,
        SQLException failure) {
        metrics.recordSql(sql, elapsed, failure, false);
    }

    @Override
    public void onAfterExecuteBatch(StatementInformation info, long elapsed, int[] counts, SQLException failure) {
        metrics.recordSql(info.getSql(), elapsed, failure, true);
    }

    @Override
    public void onBeforeCommit(ConnectionInformation connection) {
        metrics.beforeJdbcCommit();
    }

    @Override
    public void onAfterCommit(ConnectionInformation connection, long elapsed, SQLException failure) {
        metrics.afterJdbcCompletion(failure);
    }

    @Override
    public void onAfterRollback(ConnectionInformation connection, long elapsed, SQLException failure) {
        metrics.afterJdbcCompletion(failure);
    }
}
