-- 이메일 변경 화면에서 이메일 인증 세션을 분리하기 위해 CHANGE_EMAIL purpose 를 허용한다.
-- 기존 REGISTER / PASSWORD_RESET 토큰과 섞이지 않도록 purpose check constraint 를 함께 갱신한다.
ALTER TABLE email_verification
    DROP CONSTRAINT IF EXISTS email_verification_purpose_check;

ALTER TABLE email_verification
    ADD CONSTRAINT email_verification_purpose_check
        CHECK (purpose IN ('REGISTER', 'PASSWORD_RESET', 'CHANGE_EMAIL'));
