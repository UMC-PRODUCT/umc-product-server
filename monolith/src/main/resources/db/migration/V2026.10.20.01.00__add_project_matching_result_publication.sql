-- 기존 차수의 발표 시각과 공개 완료 시각은 null을 유지합니다.
ALTER TABLE project_matching_round
    ADD COLUMN result_publish_at TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN result_published_at TIMESTAMP(6) WITH TIME ZONE;

ALTER TABLE project_member
    ADD COLUMN assigned_matching_round_id BIGINT REFERENCES project_matching_round (id);

-- 기존 멤버는 상태를 유지하고, 연결된 지원서로 assigned_matching_round_id만 채웁니다.
UPDATE project_member pm
SET assigned_matching_round_id = pa.applied_matching_round_id
FROM project_application pa
WHERE pm.project_application_id = pa.id;

CREATE INDEX ix_project_member_pending_publication
    ON project_member (assigned_matching_round_id, id)
    WHERE status = 'PENDING_PUBLICATION';

CREATE INDEX ix_project_matching_round_pending_publication
    ON project_matching_round (result_publish_at, id)
    WHERE result_publish_at IS NOT NULL AND result_published_at IS NULL;
