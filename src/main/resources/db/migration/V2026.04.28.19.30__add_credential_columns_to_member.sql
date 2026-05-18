-- ID/PW 로그인 도입을 위한 member 테이블 자격증명 컬럼 추가.
--
-- 기존 OAuth 전용 회원과의 호환을 위해 nullable로 둔다.
-- password_hash 는 Spring DelegatingPasswordEncoder 표준에 따라
-- "{id}encoded" prefix 를 포함한 단일 컬럼으로 저장한다.

-- 1) 자격증명 컬럼 추가 (둘 다 nullable: OAuth 전용 회원 호환)
ALTER TABLE member
    ADD COLUMN login_id      VARCHAR(20),
    ADD COLUMN password_hash VARCHAR(255);

-- 2) login_id 유니크 인덱스 (NULL 허용 + 다중 NULL 허용은 PostgreSQL 기본 동작)
CREATE UNIQUE INDEX uk_member_login_id
    ON member (login_id);

-- 3) login_id 형식 가드 (영문/숫자/._- , 5~20자)
--    상세 형식 검증은 Application 계층에서 수행하지만,
--    DB 레벨에서도 최소 가드를 둔다.
ALTER TABLE member
    ADD CONSTRAINT member_login_id_format_check
        CHECK (login_id IS NULL OR login_id ~ '^[A-Za-z0-9._-]{5,20}$');
