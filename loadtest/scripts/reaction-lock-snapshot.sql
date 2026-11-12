-- 각 watch 실행은 별도 읽기 전용 트랜잭션이다. 하나의 긴 통계 스냅샷을 재사용하지 않는다.
SET timezone = 'UTC';
\if :{?test_id}
\else
\set test_id unassigned
\endif

WITH observed AS MATERIALIZED (
  SELECT * FROM pg_stat_activity
  WHERE datname = current_database() AND pid <> pg_backend_pid()
    AND (backend_type = 'client backend' OR backend_type IS NULL)
), classified AS MATERIALIZED (
  SELECT *,
         CASE
           WHEN query ~* '\mcommunity_thread_member\M' THEN 'community_thread_member'
           WHEN query ~* '\mcommunity_thread\M' THEN 'community_thread'
           WHEN query ~* '\mchat_message_reaction\M' THEN 'chat_message_reaction'
           WHEN query ~* '\mchat_message\M' THEN 'chat_message'
           WHEN query ~* '\mchat_member\M' THEN 'chat_member'
           WHEN query ~* '\mchat_room\M' THEN 'chat_room'
           WHEN query ~* '\mevent_outbox\M' THEN 'event_outbox'
           ELSE 'other'
         END AS inferred_table,
         CASE
           WHEN query ~* '\mfor\s+(no\s+key\s+)?update\M' THEN 'select_for_update'
           WHEN regexp_replace(query, '/\*.*?\*/', '', 'g') ~* '^\s*update\M' THEN 'update'
           WHEN regexp_replace(query, '/\*.*?\*/', '', 'g') ~* '^\s*insert\M' THEN 'insert'
           WHEN regexp_replace(query, '/\*.*?\*/', '', 'g') ~* '^\s*delete\M' THEN 'delete'
           WHEN regexp_replace(query, '/\*.*?\*/', '', 'g') ~* '^\s*select\M' THEN 'select'
           ELSE 'other'
         END AS statement_kind
  FROM observed
), activity AS MATERIALIZED (
  SELECT *,
         CASE WHEN wait_event_type = 'Lock' THEN pg_blocking_pids(pid) ELSE ARRAY[]::integer[] END AS blockers
  FROM classified
  WHERE backend_type = 'client backend' AND state <> 'idle'
), waiting AS (
  SELECT pid, min(waitstart) AS wait_started_at, array_agg(DISTINCT locktype) AS lock_types
  FROM pg_locks WHERE NOT granted GROUP BY pid
)
SELECT jsonb_build_object(
  'schemaVersion', 2, 'testId', :'test_id',
  'sampleAt', clock_timestamp(), 'database', current_database(),
  'unreadableSessions', (SELECT count(*) FROM observed
    WHERE state IS NULL OR query IS NULL OR query = '<insufficient privilege>' OR state = 'disabled'),
  'lockWaiters', count(*) FILTER (WHERE a.wait_event_type = 'Lock'),
  'blockerSessions', (SELECT coalesce(jsonb_agg(jsonb_build_object(
    'pid', b.pid, 'backendStartedAt', b.backend_start, 'state', b.state,
    'inferredTable', b.inferred_table, 'statementKind', b.statement_kind,
    'queryStartedAt', b.query_start, 'transactionStartedAt', b.xact_start,
    'queryIsLastStatement', b.state <> 'active'
  )), '[]'::jsonb) FROM classified b
    WHERE b.pid IN (SELECT unnest(blockers) FROM activity)),
  'sessions', coalesce(jsonb_agg(jsonb_build_object(
    'pid', a.pid, 'backendStartedAt', a.backend_start, 'state', a.state,
    'queryKind', a.inferred_table || '_' || a.statement_kind,
    'inferredTable', a.inferred_table, 'statementKind', a.statement_kind,
    'queryId', a.query_id::text, 'queryIsLastStatement', a.state <> 'active',
    'waitEventType', a.wait_event_type, 'waitEvent', a.wait_event, 'blockingPids', a.blockers,
    'queryStartedAt', a.query_start, 'transactionStartedAt', a.xact_start,
    'queryAgeMs', round(extract(epoch FROM clock_timestamp() - a.query_start) * 1000),
    'waitStartedAt', w.wait_started_at,
    'waitLockTypes', w.lock_types,
    'waitAgeMs', round(extract(epoch FROM clock_timestamp() - w.wait_started_at) * 1000)
  )) FILTER (WHERE a.pid IS NOT NULL), '[]'::jsonb)
)
FROM activity a LEFT JOIN waiting w USING (pid)
\watch :watch_seconds c=:sample_count
