-- 기존 차수는 발표 시각을 별도로 설정하지 않았으므로 null을 유지합니다.
-- 새 차수의 발표 시각은 생성 요청에서 필수로 받으며, 기존 차수는 수정 API로 설정할 수 있습니다.
ALTER TABLE project_matching_round
    ADD COLUMN result_publish_at TIMESTAMP(6) WITH TIME ZONE;
