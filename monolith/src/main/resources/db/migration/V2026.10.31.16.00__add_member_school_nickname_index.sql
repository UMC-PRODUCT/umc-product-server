-- 공개 닉네임 중복 조회를 위한 비유일 인덱스. 기존 중복 데이터는 허용한다.
CREATE INDEX idx_member_school_id_nickname ON member (school_id, nickname);
