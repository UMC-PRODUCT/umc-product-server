-- 각 watch 실행은 별도 읽기 전용 트랜잭션이다. 하나의 긴 통계 스냅샷을 재사용하지 않는다.
SET timezone = 'UTC';

WITH activity AS MATERIALIZED (
  SELECT pid, state, wait_event_type, wait_event, query_start, xact_start,
         CASE
           WHEN query ~* '\mcommunity_thread\M' AND query ~* '\mfor\s+(no\s+key\s+)?update\M'
             THEN 'community_thread_for_update'
           WHEN query ~* '\mchat_room\M' AND query ~* '\mfor\s+(no\s+key\s+)?update\M'
             THEN 'chat_room_for_update'
           WHEN query ~* '\mchat_message_reaction\M' THEN 'reaction'
           WHEN query ~* '\mcommunity_thread\M' THEN 'community_thread'
           WHEN query ~* '\mchat_room\M' THEN 'chat_room'
           WHEN query ~* '\mevent_outbox\M' THEN 'outbox'
           ELSE 'other'
         END AS query_kind,
         CASE WHEN wait_event_type = 'Lock' THEN pg_blocking_pids(pid) ELSE ARRAY[]::integer[] END AS blockers
  FROM pg_stat_activity
  WHERE datname = current_database() AND pid <> pg_backend_pid()
    AND backend_type = 'client backend' AND state <> 'idle'
), waiting AS (
  SELECT pid, min(waitstart) AS wait_started_at FROM pg_locks WHERE NOT granted GROUP BY pid
)
SELECT jsonb_build_object(
  'sampleAt', clock_timestamp(), 'database', current_database(),
  'lockWaiters', count(*) FILTER (WHERE a.wait_event_type = 'Lock'),
  'sessions', coalesce(jsonb_agg(jsonb_build_object(
    'pid', a.pid, 'state', a.state, 'queryKind', a.query_kind,
    'waitEventType', a.wait_event_type, 'waitEvent', a.wait_event, 'blockingPids', a.blockers,
    'queryStartedAt', a.query_start, 'transactionStartedAt', a.xact_start,
    'queryAgeMs', round(extract(epoch FROM clock_timestamp() - a.query_start) * 1000),
    'waitStartedAt', w.wait_started_at,
    'waitAgeMs', round(extract(epoch FROM clock_timestamp() - w.wait_started_at) * 1000)
  )) FILTER (WHERE a.pid IS NOT NULL), '[]'::jsonb)
)
FROM activity a LEFT JOIN waiting w USING (pid)
\watch :watch_seconds c=:sample_count
