-- 1. 기존 unique 제약 제거
ALTER TABLE challenger_workbook
    DROP CONSTRAINT IF EXISTS uk_challenger_workbook_challenger_id_original_workbook_id;

-- 2. status CHECK 제약 제거
ALTER TABLE challenger_workbook
    DROP CONSTRAINT IF EXISTS challenger_workbook_status_check;

-- 3. challenger_id → member_id 로 rename, 불필요 컬럼 DROP
ALTER TABLE challenger_workbook
    RENAME COLUMN challenger_id TO member_id;

ALTER TABLE challenger_workbook
    DROP COLUMN IF EXISTS schedule_id,
    DROP COLUMN IF EXISTS status,
    DROP COLUMN IF EXISTS best_reason,
    DROP COLUMN IF EXISTS feedback,
    DROP COLUMN IF EXISTS submission;

-- 4. 신규 컬럼 추가
ALTER TABLE challenger_workbook
    ADD COLUMN study_group_id            bigint, -- nullable (워크북 강제 배포 등을 고려)
    ADD COLUMN content                   text, -- nullable
    ADD COLUMN is_excused                boolean,
    ADD COLUMN excused_reason            text,
    ADD COLUMN excuse_approved_member_id bigint;

-- 5. 기존 row 기본값 채우기
UPDATE challenger_workbook
SET is_excused = false;

-- 6. NOT NULL 설정
ALTER TABLE challenger_workbook
    ALTER COLUMN is_excused SET NOT NULL;

-- 7. 새 unique 제약 추가
ALTER TABLE challenger_workbook
    ADD CONSTRAINT uk_challenger_workbook_member_id_original_workbook_id
        UNIQUE (member_id, original_workbook_id);
