-- 신규·기존 챌린저 공통 설문 대상을 추가하고 전용 대상은 유지한다.
ALTER TABLE user_feedback_template
    DROP CONSTRAINT user_feedback_template_target_type_check;

ALTER TABLE user_feedback_template
    ADD CONSTRAINT user_feedback_template_target_type_check CHECK (target_type IN (
        'CHALLENGER',
        'NEW_CHALLENGER',
        'EXPERIENCED_CHALLENGER',
        'ADMIN'
    ));
