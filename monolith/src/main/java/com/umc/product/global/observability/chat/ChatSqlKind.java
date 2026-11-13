package com.umc.product.global.observability.chat;

import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record ChatSqlKind(String table, String statement) {

    private static final Pattern TABLE = Pattern.compile(
        "\\b(?:from|update|into|join)\\s+(?:[\\w\"]+\\.)?\"?"
            + "(community_thread_member|community_thread|chat_message_reaction|chat_message|chat_member|chat_room"
            + "|event_outbox|report_reason|report)"
            + "\"?\\b", Pattern.CASE_INSENSITIVE
    );
    private static final Pattern OPERATION = Pattern.compile("^\\s*(select|update|insert|delete)\\b",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern LOCK = Pattern.compile("\\bfor\\s+(?:no\\s+key\\s+)?update\\b",
        Pattern.CASE_INSENSITIVE);

    static ChatSqlKind from(String sql) {
        String query = sql == null ? "" : sql.replaceAll("(?s)/\\*.*?\\*/", "");
        Matcher table = TABLE.matcher(query);
        Matcher operation = OPERATION.matcher(query);
        String statement = operation.find() ? operation.group(1).toLowerCase(Locale.ROOT) : "other";
        if (statement.equals("select") && LOCK.matcher(query).find()) {
            statement = "select_for_update";
        }
        return new ChatSqlKind(table.find() ? table.group(1).toLowerCase(Locale.ROOT) : "other", statement);
    }

    static String outcome(SQLException failure) {
        if (failure == null) {
            return "success";
        }
        String state = failure.getSQLState();
        if (state == null) {
            return "error";
        }
        return switch (state) {
            case "55P03" -> "lock_not_available";
            case "40P01" -> "deadlock";
            case "40001" -> "serialization_failure";
            case "57014" -> "query_cancelled";
            case "23505" -> "unique_violation";
            default -> "error";
        };
    }
}
