-- 전용 시드 방에서 부하와 후속 처리가 끝난 뒤 실행한다. 최종 스냅샷으로 중간 상태의 단조성을 증명하지 않는다.
WITH requested AS (
  SELECT jsonb_array_elements_text(:'thread_ids'::jsonb)::bigint AS id
), threads AS MATERIALIZED (
  SELECT t.id, t.chat_room_id, t.last_message_id
  FROM community_thread t JOIN requested r USING (id)
), messages AS MATERIALIZED (
  SELECT m.* FROM chat_message m JOIN threads t ON t.chat_room_id = m.room_id
), duplicate_messages AS (
  SELECT room_id, sender_member_id, client_message_id FROM messages
  WHERE client_message_id IS NOT NULL
  GROUP BY room_id, sender_member_id, client_message_id HAVING count(*) > 1
), duplicate_reactions AS (
  SELECT r.message_id, r.member_id, r.emoji FROM chat_message_reaction r
  JOIN messages m ON m.id = r.message_id
  GROUP BY r.message_id, r.member_id, r.emoji HAVING count(*) > 1
), members AS (
  SELECT t.id AS thread_id, t.chat_room_id, c.member_id, c.unread_count,
    cm.id AS chat_member_id, cm.last_read_message_id,
    (SELECT count(*) FROM messages m WHERE m.room_id = t.chat_room_id
      AND m.id > coalesce(cm.last_read_message_id, 0)) AS expected_unread
  FROM threads t JOIN community_thread_member c ON c.thread_id = t.id AND c.state = 'ACTIVE'
  LEFT JOIN chat_member cm ON cm.room_id = t.chat_room_id AND cm.member_id = c.member_id
), counts AS (
  SELECT
    (SELECT count(*) FROM requested) - (SELECT count(*) FROM threads) AS missing_threads,
    (SELECT count(*) FROM duplicate_messages) AS duplicate_messages,
    (SELECT count(*) FROM duplicate_reactions) AS duplicate_reactions,
    (SELECT count(*) FROM members WHERE chat_member_id IS NULL) AS missing_members,
    (SELECT count(*) FROM members WHERE unread_count <> expected_unread) AS unread_mismatches,
    (SELECT count(*) FROM members c WHERE last_read_message_id IS NOT NULL AND NOT EXISTS (
      SELECT 1 FROM messages m WHERE m.id = c.last_read_message_id AND m.room_id = c.chat_room_id
    )) AS invalid_read_positions,
    (SELECT count(*) FROM threads t WHERE t.last_message_id IS DISTINCT FROM
      (SELECT max(m.id) FROM messages m WHERE m.room_id = t.chat_room_id)) AS last_message_mismatches
)
SELECT jsonb_build_object(
  'testId', :'test_id', 'runId', :'run_id', 'checkedAt', clock_timestamp(), 'database', current_database(),
  'threadIds', :'thread_ids'::jsonb, 'scope', 'final_state_only',
  'measuredMessageRows', CASE WHEN :'run_id' = '' THEN NULL ELSE (
    SELECT count(*) FROM messages WHERE
      CASE WHEN content IS JSON OBJECT THEN content::jsonb #>> '{__k6_chat,runId}' END = :'run_id'
      AND CASE WHEN content IS JSON OBJECT THEN content::jsonb #>> '{__k6_chat,kind}' END = 'message'
  ) END,
  'violations', to_jsonb(c),
  'passed', missing_threads = 0 AND duplicate_messages = 0 AND duplicate_reactions = 0
    AND missing_members = 0 AND unread_mismatches = 0 AND invalid_read_positions = 0
    AND last_message_mismatches = 0
) FROM counts c;
