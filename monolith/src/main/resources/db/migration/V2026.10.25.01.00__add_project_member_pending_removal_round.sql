-- 기존 공개 명단을 유지하면서 랜덤 매칭 최종 발표에 적용할 해제를 예약한다.
ALTER TABLE project_member
    ADD COLUMN pending_removal_round_id BIGINT REFERENCES project_matching_round (id),
    ADD CONSTRAINT ck_project_member_pending_removal_active
        CHECK (pending_removal_round_id IS NULL OR status = 'ACTIVE');

CREATE INDEX idx_project_member_pending_removal_round
    ON project_member (pending_removal_round_id)
    WHERE pending_removal_round_id IS NOT NULL;
