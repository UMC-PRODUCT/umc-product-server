-- 기존 프로젝트의 제출 시각은 기록이 없어 임의로 보정하지 않습니다.
ALTER TABLE project ADD COLUMN submitted_at TIMESTAMP(6) WITH TIME ZONE;
