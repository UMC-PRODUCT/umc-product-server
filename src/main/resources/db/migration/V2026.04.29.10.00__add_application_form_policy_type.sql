-- Project 도메인 PR2 — 지원 폼 섹션 타입 도입.
-- 섹션을 COMMON(전체 파트 노출) | PART(allowed_parts 명시) 두 종으로 구분하여
-- ChallengerPart enum 변경에도 의도가 보존되도록 한다.

-- 1) type 컬럼 추가 — 기존 row는 모두 part 한정 의도였으므로 'PART'로 백필
ALTER TABLE project_application_form_policy
    ADD COLUMN type VARCHAR(255);

UPDATE project_application_form_policy
SET type = 'PART'
WHERE type IS NULL;

ALTER TABLE project_application_form_policy
    ALTER COLUMN type SET NOT NULL;

-- 2) allowed_parts 를 nullable 로 완화 (COMMON 일 때는 의미 없음)
ALTER TABLE project_application_form_policy
    ALTER COLUMN allowed_parts DROP NOT NULL;

-- 3) type 값 도메인 제약
ALTER TABLE project_application_form_policy
    ADD CONSTRAINT chk_policy_type_value CHECK (type IN ('COMMON', 'PART'));

-- 4) type 과 allowed_parts 의 일관성 제약
--    COMMON  → allowed_parts 비어있거나 NULL
--    PART    → allowed_parts 1개 이상
ALTER TABLE project_application_form_policy
    ADD CONSTRAINT chk_policy_part_consistency CHECK (
        (type = 'COMMON' AND (allowed_parts IS NULL OR cardinality(allowed_parts) = 0))
        OR
        (type = 'PART' AND cardinality(allowed_parts) >= 1)
    );
