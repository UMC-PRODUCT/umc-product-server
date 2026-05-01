-- 1. notice_vote 테이블에 투표 기간 컬럼 추가 (NULL 허용으로 시작)
ALTER TABLE notice_vote
    ADD COLUMN starts_at TIMESTAMPTZ,
    ADD COLUMN ends_at_exclusive TIMESTAMPTZ;

-- 2. 기존 데이터 복사 (form 테이블에서 기간 데이터를 가져와 notice_vote로 마이그레이션)
-- notice_vote.vote_id가 form.id와 매칭
UPDATE notice_vote nv
SET starts_at = f.starts_at,
    ends_at_exclusive = f.ends_at_exclusive
FROM form f
WHERE nv.vote_id = f.id;

-- 3. 데이터 복사 완료 후 NOT NULL 제약 조건 부여
-- 투표는 반드시 기간이 설정되어야 하므로 NOT NULL
ALTER TABLE notice_vote
    ALTER COLUMN starts_at SET NOT NULL,
    ALTER COLUMN ends_at_exclusive SET NOT NULL;
