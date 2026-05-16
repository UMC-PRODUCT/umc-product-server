-- login_id 형식 정책 강화: 영문 대문자 허용을 제거하고 소문자만 허용한다.
-- Application 계층의 CredentialPolicy.LOGIN_ID_PATTERN 과 일치해야 한다.

ALTER TABLE member
    DROP CONSTRAINT IF EXISTS member_login_id_format_check;

ALTER TABLE member
    ADD CONSTRAINT member_login_id_format_check
        CHECK (login_id IS NULL OR login_id ~ '^[a-z0-9._-]{5,20}$');
