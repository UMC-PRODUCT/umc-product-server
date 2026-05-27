-- 이메일 인증 코드 brute-force 방어를 위한 시도 횟수 컬럼을 추가한다.
-- 임계치(5회) 초과 시 도메인 레이어에서 세션을 무효화한다.
ALTER TABLE email_verification
    ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0;
