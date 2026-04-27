ALTER TABLE notice_target ADD COLUMN min_target_role text NOT NULL DEFAULT 'CHALLENGER';

ALTER TABLE notice_target ALTER COLUMN min_target_role DROP DEFAULT;

-- local에만 존재하는 컬럼이므로 IF EXISTS로 처리
ALTER TABLE notice_target DROP COLUMN IF EXISTS target_staff_roles;
