CREATE UNIQUE INDEX uq_gisu_active
    ON gisu (is_active)
    WHERE is_active = true;
