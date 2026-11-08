-- ALL은 설문 대상 구분이며 시스템 권한을 추가하지 않는다.
ALTER TABLE user_feedback_template
    DROP CONSTRAINT user_feedback_template_target_type_check;

ALTER TABLE user_feedback_template
    ADD CONSTRAINT user_feedback_template_target_type_check CHECK (target_type IN (
        'ALL',
        'CHALLENGER',
        'NEW_CHALLENGER',
        'EXPERIENCED_CHALLENGER',
        'ADMIN'
    ));

-- 이번에 생성한 활성 매칭 템플릿의 대상만 모든 로그인 사용자로 변경한다.
-- 기존 Form·문항·선택지·응답과 연결된 ID, 비활성 과거 템플릿은 유지한다.
UPDATE user_feedback_template
SET target_type = 'ALL',
    updated_at = NOW()
WHERE context = 'MATCHING_COMPLETED'
  AND target_type = 'CHALLENGER'
  AND is_active = true;
