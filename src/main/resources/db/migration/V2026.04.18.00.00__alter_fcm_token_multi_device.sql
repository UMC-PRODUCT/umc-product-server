-- fcm_token 테이블 멀티 디바이스 지원을 위한 변경
-- 기존: member_id 기준 1:1 (토큰 갱신 시 덮어쓰기)
-- 변경: member_id 기준 1:N, is_active로 기기 비활성화 관리

-- 1. is_active 컬럼 추가 (기존 레코드는 모두 활성 상태로 초기화)
ALTER TABLE fcm_token
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- 2. UNIQUE(member_id, fcm_token) 제약 추가 (동일 기기 중복 등록 방지)
--    member_id가 NULL인 경우는 제약 대상에서 제외됨 (PostgreSQL UNIQUE는 NULL 허용)
CREATE UNIQUE INDEX uix_fcm_token_member_token ON fcm_token (member_id, fcm_token)
    WHERE member_id IS NOT NULL;

-- 3. 활성 토큰 조회 성능을 위한 인덱스
CREATE INDEX ix_fcm_token_member_id_active ON fcm_token (member_id, is_active);
