CREATE TABLE product_team_generation (
    id BIGSERIAL PRIMARY KEY,
    generation BIGINT NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_product_team_generation_generation UNIQUE (generation),
    CONSTRAINT ck_product_team_generation_period CHECK (start_at < end_at)
);

CREATE UNIQUE INDEX uk_product_team_generation_active
    ON product_team_generation (is_active)
    WHERE is_active = TRUE;

CREATE TABLE product_team_member (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    introduction VARCHAR(2000),
    profile_image_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_product_team_member_member_id UNIQUE (member_id)
);

CREATE TABLE product_team_membership (
    id BIGSERIAL PRIMARY KEY,
    product_team_member_id BIGINT NOT NULL REFERENCES product_team_member(id) ON DELETE CASCADE,
    product_team_generation_id BIGINT NOT NULL REFERENCES product_team_generation(id),
    part VARCHAR(32) NOT NULL,
    role VARCHAR(32) NOT NULL,
    position VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_product_team_membership_activity
        UNIQUE (product_team_member_id, product_team_generation_id, part, role, position)
);

CREATE INDEX ix_product_team_membership_generation_part
    ON product_team_membership (product_team_generation_id, part);

CREATE INDEX ix_product_team_membership_member
    ON product_team_membership (product_team_member_id);

CREATE UNIQUE INDEX uk_product_team_lead_per_generation
    ON product_team_membership (product_team_generation_id, role)
    WHERE role IN ('PRODUCT_LEAD', 'PRODUCT_VICE_LEAD');

CREATE UNIQUE INDEX uk_product_team_leader_per_part
    ON product_team_membership (product_team_generation_id, part)
    WHERE role = 'TEAM_LEADER';
