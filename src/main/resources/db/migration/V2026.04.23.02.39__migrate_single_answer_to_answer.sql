-- Legacy single_answer → answer / answer_choice 데이터 이관

-- 운영 환경의 기존 투표 응답(single_answer.value jsonb) 데이터를 신규 정규화 구조(answer + answer_choice)로 이관한다.
--
-- single_answer.value 형태:
--   - RADIO / DROPDOWN: {"selectedOptionId": <long>}
--   - CHECKBOX       : {"selectedOptionIds": [<long>, <long>, ...]}
--   - SHORT_TEXT / LONG_TEXT / SCHEDULE / PORTFOLIO / PREFERRED_PART: 운영 데이터 없음 → 건너뜀
--
-- single_answer 테이블은 이 마이그레이션에서 삭제하지 않는다.
-- 롤백 여지 확보용으로 유지. DROP은 후속 PR에서 별도 마이그레이션으로 진행.

-- 1. 임시 매핑 컬럼: single_answer.id <-> answer.id 연결 유지
ALTER TABLE answer
    ADD COLUMN temp_legacy_single_answer_id BIGINT;

-- 2. single_answer → answer 복사 (RADIO / DROPDOWN / CHECKBOX 만)
INSERT INTO answer (
    form_response_id,
    question_id,
    answered_as_type,
    created_at,
    updated_at,
    temp_legacy_single_answer_id
)
SELECT
    sa.response_id,
    sa.question_id,
    sa.answered_as_type,
    sa.created_at::timestamp,
    sa.updated_at::timestamp,
    sa.id
FROM single_answer sa
WHERE sa.answered_as_type IN ('RADIO', 'DROPDOWN', 'CHECKBOX');

-- 3-A. RADIO / DROPDOWN: value.selectedOptionId 1개 → answer_choice 1 row
INSERT INTO answer_choice (answer_id, answered_as_content, question_option_id)
SELECT
    a.id,
    qo.content,
    qo.id
FROM answer a
    JOIN single_answer sa ON sa.id = a.temp_legacy_single_answer_id
    JOIN question_option qo
        ON qo.id = (sa.value ->> 'selectedOptionId')::BIGINT
WHERE a.answered_as_type IN ('RADIO', 'DROPDOWN');

-- 3-B. CHECKBOX: value.selectedOptionIds 배열 펼쳐 answer_choice N rows
INSERT INTO answer_choice (answer_id, answered_as_content, question_option_id)
SELECT
    a.id,
    qo.content,
    qo.id
FROM answer a
    JOIN single_answer sa ON sa.id = a.temp_legacy_single_answer_id
    CROSS JOIN LATERAL jsonb_array_elements_text(sa.value -> 'selectedOptionIds')
        AS opt(option_id_str)
    JOIN question_option qo ON qo.id = opt.option_id_str::BIGINT
WHERE a.answered_as_type = 'CHECKBOX';

-- 4. 방어적 정리: choice 없는 answer 제거
-- malformed value 또는 삭제된 option에 대응. 정상 데이터면 0건 삭제.
DELETE FROM answer
WHERE answered_as_type IN ('RADIO', 'DROPDOWN', 'CHECKBOX')
  AND temp_legacy_single_answer_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM answer_choice ac WHERE ac.answer_id = answer.id
  );

-- 5. 임시 매핑 컬럼 제거
ALTER TABLE answer
    DROP COLUMN temp_legacy_single_answer_id;
