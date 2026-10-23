-- 만료 세션 정리 잡과 만료 검사 쿼리의 효율을 위해 expires_at 인덱스를 추가한다.
-- PostgreSQL CONCURRENTLY 옵션은 Flyway 트랜잭션과 충돌하므로 사용하지 않는다.
CREATE INDEX IF NOT EXISTS idx_email_verification_expires_at
    ON email_verification (expires_at);
