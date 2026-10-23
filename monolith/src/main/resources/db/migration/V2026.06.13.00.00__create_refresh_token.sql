CREATE TABLE refresh_token
(
    id         BIGSERIAL PRIMARY KEY,
    jti        UUID        NOT NULL UNIQUE,
    member_id  BIGINT      NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_token_member_id ON refresh_token (member_id);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token (expires_at);
