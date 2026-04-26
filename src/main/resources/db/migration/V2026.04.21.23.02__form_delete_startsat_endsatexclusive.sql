-- 데이터가 notice_vote로 모두 복사된 후 form 테이블에서 관련 컬럼을 삭제합니다.
ALTER TABLE form
    DROP COLUMN ends_at_exclusive,
    DROP COLUMN starts_at;

-- 투표 도메인이 분리되면서 기존의 제약 조건을 정리합니다.
ALTER TABLE question
    DROP CONSTRAINT IF EXISTS question_type_check;
