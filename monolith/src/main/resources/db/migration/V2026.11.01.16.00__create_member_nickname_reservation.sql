-- 수료 후 탈퇴한 회원의 닉네임만 보존한다. 기존 회원이나 중복 닉네임은 변경하지 않는다.
CREATE TABLE member_nickname_reservation (
    school_id BIGINT NOT NULL,
    nickname VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (school_id, nickname)
);
