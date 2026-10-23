-- ADR-017 후속 마이그레이션. 로그인 식별자를 loginId 에서 email 로 전환한 뒤,
-- 더 이상 사용되지 않는 member.login_id 컬럼과 관련 CHECK 제약을 제거한다.
--
-- 사전 조건:
--   - 코드 레벨에서 Member.loginId 필드 및 관련 메서드가 모두 제거되어 있어야 한다.
--   - 운영 환경에서는 이전 버전 코드가 더 이상 동작하지 않는 것을 확인한 뒤 적용한다.

-- 1) login_id 와 password_hash 의 공존 여부를 강제하던 제약 제거
ALTER TABLE member
    DROP CONSTRAINT IF EXISTS member_credentials_coexistence_check;

-- 2) login_id 형식을 강제하던 CHECK 제약 제거
ALTER TABLE member
    DROP CONSTRAINT IF EXISTS member_login_id_format_check;

-- 3) login_id UNIQUE 인덱스 제거 (DROP COLUMN 으로 같이 제거되지만 명시적으로 정리)
DROP INDEX IF EXISTS uk_member_login_id;

-- 4) login_id 컬럼 제거
ALTER TABLE member
    DROP COLUMN IF EXISTS login_id;
