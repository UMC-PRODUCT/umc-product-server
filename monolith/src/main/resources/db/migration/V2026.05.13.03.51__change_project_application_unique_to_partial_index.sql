-- 지원 철회(CANCELLED) soft delete 도입으로 (form, round, applicant) UK 를 active 상태(DRAFT/SUBMITTED)에서만 적용하도록 완화. (#849)
--
-- 기존: 한 (form, round, applicant) 키에 row 1 개만 허용 -> cancel 후 동일 차수 재지원이 DB 레벨에서 막힘.
-- 변경: status IN ('DRAFT','SUBMITTED') 인 행만 unique 영역에 포함 -> CANCELLED 행은 인덱스 범위 밖, 동일 키로 새 DRAFT 가능.
--       활성 지원서(DRAFT/SUBMITTED)는 partial unique index 가 항상 1 개로 강제.
--
-- 주의: JPA @UniqueConstraint는 WHERE 절을 표현할 수 없으므로 Entity 의 @Table(uniqueConstraints=...) 은 제거. 이 partial unique index 로만 관리됨.

ALTER TABLE project_application
    DROP CONSTRAINT IF EXISTS uk_project_application_form_member_matching_round;

CREATE UNIQUE INDEX IF NOT EXISTS uk_project_application_active_form_round_applicant
    ON project_application (project_application_form_id, applied_matching_round_id, applicant_member_id)
    WHERE status IN ('DRAFT', 'SUBMITTED');
