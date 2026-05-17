-- 회원의 로그인 식별자를 loginId 에서 email 로 전환하기 위한 사전 작업.
-- 자세한 결정 배경은 docs/adr/017-email-as-login-identifier.md 를 참고한다.
--
-- 본 마이그레이션은 member.email 에 UNIQUE 제약을 추가한다.
-- 운영 DB 에 동일 email 중복 회원이 존재하면 본 마이그레이션은 실패한다.
-- 따라서 마이그레이션 적용 전 아래 SQL 로 중복 여부를 사전 확인하고,
-- 발견 시 도메인/운영 정책에 따라 별도로 정리해야 한다.
--
--   SELECT email, COUNT(*) AS cnt
--   FROM member
--   GROUP BY email
--   HAVING COUNT(*) > 1;

ALTER TABLE member
    ADD CONSTRAINT uk_member_email UNIQUE (email);
