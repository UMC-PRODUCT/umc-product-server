-- ENUM Array를 번호로 저장하고 있음 .. String 배열로 변경
ALTER TABLE notice_target
ALTER
COLUMN target_challenger_part TYPE text[]
    USING target_challenger_part::text[];
