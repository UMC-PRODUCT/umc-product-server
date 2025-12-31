package com.umc.product.global.config; // 패키지명 확인

import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import jakarta.annotation.PostConstruct;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class P6SpyConfig {

    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(P6SpyFormatter.class.getName());
    }

    // 내부 클래스로 포맷터 정의
    public static class P6SpyFormatter implements MessageFormattingStrategy {

        @Override
        public String formatMessage(int connectionId, String now, long elapsed, String category,
                                    String prepared, String sql, String url) {
            sql = formatSql(category, sql);
            // [실행시간] | SQL 문법
            return String.format("[%s] | %d ms | %s", category, elapsed, formatSql(category, sql));
        }

        private String formatSql(String category, String sql) {
            if (sql == null || sql.trim().isEmpty()) {
                return sql;
            }

            // Only format Statement, PreparedStatement
            if ("statement".equals(category)) {
                String trimmedSQL = sql.trim().toLowerCase(Locale.ROOT);
                if (trimmedSQL.startsWith("create") || trimmedSQL.startsWith("alter")
                        || trimmedSQL.startsWith("comment")) {
                    sql = FormatStyle.DDL.getFormatter().format(sql);
                } else {
                    sql = FormatStyle.BASIC.getFormatter().format(sql);
                }
                return sql;
            }
            return sql;
        }
    }
}