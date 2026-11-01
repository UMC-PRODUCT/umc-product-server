-- (creator, gisu) 당 DRAFT 1개 제약을 DB 레벨에서 강제한다.
-- PostgreSQL partial unique index — DRAFT 상태에 한해서만 유일성 보장.
-- DRAFT → PENDING_REVIEW 로 전이되는 즉시 슬롯이 풀려 새 DRAFT 시작이 가능해진다.
CREATE UNIQUE INDEX uk_project_creator_gisu_draft
    ON project (created_by_member_id, gisu_id)
    WHERE status = 'DRAFT';
