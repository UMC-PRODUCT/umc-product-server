CREATE TABLE umc_product_generation (
    id BIGSERIAL PRIMARY KEY,
    generation BIGINT NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_umc_product_generation_generation UNIQUE (generation),
    CONSTRAINT ck_umc_product_generation_period CHECK (start_at < end_at)
);

CREATE UNIQUE INDEX uk_umc_product_generation_active
    ON umc_product_generation (is_active)
    WHERE is_active = TRUE;

CREATE TABLE umc_product_member (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    introduction VARCHAR(2000),
    profile_image_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_umc_product_member_member_id UNIQUE (member_id)
);

CREATE TABLE umc_product_functional_unit (
    id BIGSERIAL PRIMARY KEY,
    umc_product_generation_id BIGINT NOT NULL REFERENCES umc_product_generation(id),
    parent_unit_id BIGINT REFERENCES umc_product_functional_unit(id),
    type VARCHAR(32) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    sort_order INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_umc_product_functional_unit_code
        UNIQUE (umc_product_generation_id, type, code)
);

CREATE INDEX ix_umc_product_functional_unit_generation_type
    ON umc_product_functional_unit (umc_product_generation_id, type);

CREATE INDEX ix_umc_product_functional_unit_parent
    ON umc_product_functional_unit (parent_unit_id);

CREATE TABLE umc_product_functional_membership (
    id BIGSERIAL PRIMARY KEY,
    umc_product_member_id BIGINT NOT NULL REFERENCES umc_product_member(id) ON DELETE CASCADE,
    umc_product_generation_id BIGINT NOT NULL REFERENCES umc_product_generation(id),
    functional_unit_id BIGINT NOT NULL REFERENCES umc_product_functional_unit(id),
    role VARCHAR(32) NOT NULL,
    position VARCHAR(32) NOT NULL,
    responsibility_title VARCHAR(200),
    responsibility_description VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_umc_product_functional_membership_activity
        UNIQUE (umc_product_member_id, umc_product_generation_id, functional_unit_id, role, position, responsibility_title)
);

CREATE INDEX ix_umc_product_functional_membership_generation_unit
    ON umc_product_functional_membership (umc_product_generation_id, functional_unit_id);

CREATE INDEX ix_umc_product_functional_membership_member
    ON umc_product_functional_membership (umc_product_member_id);

CREATE UNIQUE INDEX uk_umc_product_product_lead_per_generation
    ON umc_product_functional_membership (umc_product_generation_id, role)
    WHERE role IN ('UMC_PRODUCT_LEAD', 'UMC_PRODUCT_VICE_LEAD');

CREATE UNIQUE INDEX uk_umc_product_functional_lead_per_unit
    ON umc_product_functional_membership (functional_unit_id, role)
    WHERE role IN ('CHAPTER_LEAD', 'PART_LEAD');

CREATE TABLE umc_product_squad (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    start_at TIMESTAMP WITH TIME ZONE,
    end_at TIMESTAMP WITH TIME ZONE,
    sort_order INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_umc_product_squad_code UNIQUE (code),
    CONSTRAINT ck_umc_product_squad_period CHECK (
        start_at IS NULL OR end_at IS NULL OR start_at < end_at
    )
);

CREATE INDEX ix_umc_product_squad_period
    ON umc_product_squad (start_at, end_at);

CREATE TABLE umc_product_squad_participant (
    id BIGSERIAL PRIMARY KEY,
    umc_product_squad_id BIGINT NOT NULL REFERENCES umc_product_squad(id) ON DELETE CASCADE,
    umc_product_member_id BIGINT NOT NULL REFERENCES umc_product_member(id) ON DELETE CASCADE,
    role VARCHAR(32) NOT NULL,
    position VARCHAR(32) NOT NULL,
    responsibility_title VARCHAR(200),
    responsibility_description VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_umc_product_squad_participant_activity
        UNIQUE (umc_product_squad_id, umc_product_member_id, role, position, responsibility_title)
);

CREATE INDEX ix_umc_product_squad_participant_squad
    ON umc_product_squad_participant (umc_product_squad_id);

CREATE INDEX ix_umc_product_squad_participant_member
    ON umc_product_squad_participant (umc_product_member_id);

CREATE UNIQUE INDEX uk_umc_product_squad_lead_per_squad
    ON umc_product_squad_participant (umc_product_squad_id)
    WHERE role = 'SQUAD_LEAD';
