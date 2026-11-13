-- 실행 전 준비한 방과 회원에 한정해 실제 변경 결과를 검사한다.
WITH expected AS (
  SELECT e.value AS spec, (e.value->>'threadId')::bigint AS thread_id
  FROM jsonb_array_elements(:'expectations'::jsonb) e
), membership AS (
  SELECT e.thread_id, m.value::bigint AS member_id
  FROM expected e, jsonb_array_elements_text(e.spec->'expectedMemberIds') m
), actual_members AS (
  SELECT c.thread_id, c.member_id FROM community_thread_member c
  JOIN expected e ON e.thread_id = c.thread_id WHERE c.state = 'ACTIVE'
), missing_members AS (
  SELECT * FROM membership EXCEPT SELECT * FROM actual_members
), extra_members AS (
  SELECT * FROM actual_members EXCEPT SELECT * FROM membership
), expected_reports AS (
  SELECT e.thread_id, (e.spec->>'messageId')::bigint AS message_id, r.value::bigint AS reporter_id
  FROM expected e, jsonb_array_elements_text(e.spec->'reporterIds') r
), actual_reports AS (
  SELECT r.thread_id, r.target_id AS message_id, r.reporter_id
  FROM report r JOIN expected e ON e.thread_id = r.thread_id WHERE r.target_type = 'THREAD_MESSAGE'
), missing_reports AS (
  SELECT * FROM expected_reports EXCEPT SELECT * FROM actual_reports
), extra_reports AS (
  SELECT * FROM actual_reports EXCEPT SELECT * FROM expected_reports
), violations AS (
  SELECT
    (SELECT count(*) FROM missing_members) AS missing_members,
    (SELECT count(*) FROM extra_members) AS extra_members,
    (SELECT count(*) FROM missing_reports) AS missing_reports,
    (SELECT count(*) FROM extra_reports) AS extra_reports,
    (SELECT count(*) FROM (
      SELECT thread_id, message_id, reporter_id FROM actual_reports
      GROUP BY thread_id, message_id, reporter_id HAVING count(*) <> 1
    ) duplicates) AS duplicate_reports,
    (SELECT count(*) FROM expected e
      JOIN community_thread t ON t.id = e.thread_id
      CROSS JOIN LATERAL jsonb_array_elements_text(e.spec->'readerIds') reader
      LEFT JOIN chat_member cm ON cm.room_id = t.chat_room_id AND cm.member_id = reader.value::bigint
      WHERE cm.last_read_message_id IS DISTINCT FROM (e.spec->>'messageId')::bigint
    ) AS read_position_mismatches,
    (SELECT count(*) FROM chat_message_reaction r JOIN chat_message m ON m.id = r.message_id
      JOIN community_thread t ON t.chat_room_id = m.room_id JOIN expected e ON e.thread_id = t.id
    ) AS remaining_reactions
)
SELECT jsonb_build_object('violations', to_jsonb(v), 'passed',
  NOT EXISTS (SELECT 1 FROM jsonb_each_text(to_jsonb(v)) item WHERE item.value::bigint <> 0))
FROM violations v;
