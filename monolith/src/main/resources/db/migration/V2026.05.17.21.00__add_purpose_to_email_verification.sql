-- email_verification 세션에 용도(purpose) 구분 컬럼을 추가한다.
-- 회원가입(REGISTER) 과 비밀번호 초기화(PASSWORD_RESET) 흐름의 cross-purpose 공격을 방어한다.
-- 기존 레코드는 회원가입 인증에 해당하므로 REGISTER 로 기본값을 부여한다.
ALTER TABLE email_verification
    ADD COLUMN purpose VARCHAR(20) NOT NULL DEFAULT 'REGISTER';

ALTER TABLE email_verification
    ADD CONSTRAINT email_verification_purpose_check
        CHECK (purpose IN ('REGISTER', 'PASSWORD_RESET'));
