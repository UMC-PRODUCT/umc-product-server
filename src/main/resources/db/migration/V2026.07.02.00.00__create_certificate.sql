CREATE TABLE certificate
(
    id                     BIGSERIAL PRIMARY KEY,
    created_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    serial_number          VARCHAR(40)                 NOT NULL,
    type                   VARCHAR(30)                 NOT NULL,
    status                 VARCHAR(20)                 NOT NULL,
    issuer                 VARCHAR(40)                 NOT NULL,
    recipient_member_id    BIGINT                      NOT NULL,
    recipient_name         VARCHAR(100)                NOT NULL,
    recipient_school_name  VARCHAR(100),
    gisu_id                BIGINT                      NOT NULL,
    gisu_generation        BIGINT                      NOT NULL,
    project_id             BIGINT,
    project_name           VARCHAR(100),
    merit_title            VARCHAR(100),
    merit_description      VARCHAR(500),
    issued_by_member_id    BIGINT                      NOT NULL,
    issued_at              TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    expires_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    revoked_at             TIMESTAMP(6) WITH TIME ZONE,
    revoked_by_member_id   BIGINT,
    revoke_reason          VARCHAR(500),
    file_id                VARCHAR(100)                NOT NULL,
    file_sha256            VARCHAR(64)                 NOT NULL,
    CONSTRAINT uk_certificate_serial_number UNIQUE (serial_number),
    CONSTRAINT certificate_type_check CHECK (type IN ('COMPLETION', 'MERIT', 'PROJECT_PARTICIPATION')),
    CONSTRAINT certificate_status_check CHECK (status IN ('ISSUED', 'REVOKED', 'EXPIRED')),
    CONSTRAINT certificate_issuer_check CHECK (issuer IN ('UNIVERSITY_MAKEUS_CHALLENGE', 'NEORDINARY'))
);

CREATE INDEX idx_certificate_recipient_issued_at
    ON certificate (recipient_member_id, issued_at DESC, id DESC);

CREATE INDEX idx_certificate_scope_valid
    ON certificate (type, recipient_member_id, gisu_id, project_id, merit_title, status, expires_at);
