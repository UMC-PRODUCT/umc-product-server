-- PM/운영진의 지원자 목록 화면에서 노출되는 "지원시각" / "처리시각" 컬럼을 위해
-- project_application 에 submitted_at / status_changed_at 두 시각 필드를 추가한다.
-- 둘 다 nullable -- 임시저장(PENDING) 단계에서는 null 이며, 상태 전이 시점에 도메인 메서드가 채운다.

ALTER TABLE project_application
    ADD COLUMN submitted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE project_application
    ADD COLUMN status_changed_at TIMESTAMP WITH TIME ZONE;

-- 기존 row backfill: 이미 SUBMITTED/APPROVED/REJECTED 인 row 에 대해
-- updated_at 을 잠정 시각으로 채워넣는다. 정확한 시각이 없는 과거 데이터에 대한 best-effort.
UPDATE project_application
SET submitted_at = updated_at
WHERE status IN ('SUBMITTED', 'APPROVED', 'REJECTED')
  AND submitted_at IS NULL;

UPDATE project_application
SET status_changed_at = updated_at
WHERE status IN ('APPROVED', 'REJECTED')
  AND status_changed_at IS NULL;
