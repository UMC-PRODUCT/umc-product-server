-- 프로젝트를 만든 사람을 추적한다.
-- 운영진(회장/지부장/총괄단)이 다른 PLAN 챌린저를 PO 로 지정해서 생성하는 동선이 추가되어,
-- creator(만든 운영진)와 productOwner(임명된 PM)가 분리될 수 있다.
-- DRAFT 단계 EDIT 권한을 creator 에게도 허용하고 (PROJECT-002 정책), 추적 audit 용도로 사용한다.

ALTER TABLE project ADD COLUMN created_by_member_id BIGINT;

-- 기존 row backfill: 운영진-생성 케이스가 없었으므로 PO 와 동일.
UPDATE project SET created_by_member_id = product_owner_member_id WHERE created_by_member_id IS NULL;

ALTER TABLE project ALTER COLUMN created_by_member_id SET NOT NULL;
