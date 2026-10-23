-- "한 PO당 한 기수 1 DRAFT" 정책으로 좁힌다.
-- V2026.04.25.11.00 에서 status 무관 unique 제약을 걸었으나, IN_PROGRESS/COMPLETED/ABORTED 같은
-- 종료/진행 상태에서도 같은 PO 의 새 DRAFT 생성을 차단해버려 의도와 다르게 동작했다.
-- creator 쪽 V2026.05.10.14.00 의 partial index 와 같은 패턴 (status='DRAFT' 한정) 으로 맞춘다.
-- DRAFT → PENDING_REVIEW 로 전이되는 즉시 슬롯이 풀려 같은 PO 의 새 DRAFT 시작이 가능해진다.

ALTER TABLE project DROP CONSTRAINT uk_project_owner_gisu;

CREATE UNIQUE INDEX uk_project_owner_gisu_draft
    ON project (product_owner_member_id, gisu_id)
    WHERE status = 'DRAFT';
