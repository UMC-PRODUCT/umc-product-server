-- 최종 디자인에 맞춰 활성 공통 설문의 문구만 변경한다.
-- Form·문항·선택지 ID와 기존 응답, 운영진 전용 템플릿은 유지한다.
UPDATE form f
SET title = '프로젝트 매칭 경험이 어떠셨나요?',
    updated_at = NOW()
FROM user_feedback_template t
WHERE t.form_id = f.id
  AND t.context = 'MATCHING_COMPLETED'
  AND t.target_type = 'CHALLENGER'
  AND t.is_active = true;

UPDATE question q
SET title = '새로워진 UMC 매칭 시스템의 화면 경험은 어떠셨나요?',
    updated_at = NOW()
FROM form_section s, user_feedback_template t
WHERE q.form_section_id = s.id
  AND s.form_id = t.form_id
  AND s.order_no = 1
  AND q.order_no = 2
  AND t.context = 'APPLICATION_SUBMITTED'
  AND t.target_type = 'CHALLENGER'
  AND t.is_active = true;

-- 두 설문의 척도는 어려움부터 쉬움까지의 동일한 5단계로 제공한다.
UPDATE question_option o
SET content = CASE o.order_no
        WHEN 1 THEN '어려움'
        WHEN 2 THEN '조금 어려움'
        WHEN 3 THEN '보통'
        WHEN 4 THEN '조금 쉬움'
        WHEN 5 THEN '쉬움'
    END,
    updated_at = NOW()
FROM question q, form_section s, user_feedback_template t
WHERE o.question_id = q.id
  AND q.form_section_id = s.id
  AND s.form_id = t.form_id
  AND s.order_no = 1
  AND o.order_no BETWEEN 1 AND 5
  AND t.target_type = 'CHALLENGER'
  AND t.is_active = true
  AND ((t.context = 'APPLICATION_SUBMITTED' AND q.order_no = 1)
    OR (t.context = 'MATCHING_COMPLETED' AND q.order_no = 2));

UPDATE question_option o
SET content = '매칭 차수 설정 (Admin)',
    updated_at = NOW()
FROM question q, form_section s, user_feedback_template t
WHERE o.question_id = q.id
  AND q.form_section_id = s.id
  AND s.form_id = t.form_id
  AND s.order_no = 1
  AND q.order_no = 3
  AND o.order_no = 5
  AND t.context = 'MATCHING_COMPLETED'
  AND t.target_type = 'CHALLENGER'
  AND t.is_active = true;
