-- PROJECT-006(관리 화면) 학교 운영진 scope 및 PROJECT-001 학교 필터 동작을 위해
-- 메인 PM 의 schoolId 를 Project 에 비정규화한다.
-- "Project 가 학교에 속한다"는 의미가 아니라 메인 PM 의 학교를 캐시한다는 의미.
-- 양도(PROJECT-104) 시점에 Service 가 동기화한다.

ALTER TABLE project ADD COLUMN product_owner_school_id BIGINT;

-- 기존 row backfill: 메인 PM 의 학교 ID 로 채움
UPDATE project p
SET product_owner_school_id = (
    SELECT m.school_id FROM member m WHERE m.id = p.product_owner_member_id
)
WHERE p.product_owner_school_id IS NULL;

ALTER TABLE project ALTER COLUMN product_owner_school_id SET NOT NULL;
