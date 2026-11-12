-- 탈퇴한 회원의 닉네임과 재가입 소유 키를 보존한다. 이메일 원문은 저장하지 않는다.
CREATE TABLE member_nickname_reservation (
    school_id BIGINT NOT NULL,
    nickname VARCHAR(20) NOT NULL,
    owner_key VARCHAR(64) NOT NULL,
    graduated BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (school_id, nickname, owner_key)
);

CREATE INDEX idx_member_nickname_reservation_lookup
    ON member_nickname_reservation (school_id, nickname);

CREATE INDEX idx_member_nickname_reservation_owner
    ON member_nickname_reservation (school_id, owner_key, graduated);
