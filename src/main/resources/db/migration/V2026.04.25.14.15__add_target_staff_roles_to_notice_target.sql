ALTER TABLE notice_target
    ADD COLUMN target_staff_roles text[] NOT NULL DEFAULT ARRAY ['CHALLENGER'];
