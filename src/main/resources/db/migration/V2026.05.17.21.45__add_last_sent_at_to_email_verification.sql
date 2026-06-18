-- 이메일 인증 발송 빈도 throttle 을 위한 마지막 발송 시각 컬럼을 추가한다.
-- 새 세션 생성 시점에 동일 이메일의 직전 발송으로부터 60초 이상 경과했는지 검사한다.
-- 기존 레코드의 last_sent_at 은 created_at 으로 백필해 합리적인 기본값을 부여한다.
ALTER TABLE email_verification
    ADD COLUMN last_sent_at TIMESTAMP WITH TIME ZONE;

UPDATE email_verification
SET last_sent_at = created_at
WHERE last_sent_at IS NULL;
