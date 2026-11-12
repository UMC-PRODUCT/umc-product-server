ALTER TABLE notice
    ADD COLUMN upms_notice_type VARCHAR(32);

ALTER TABLE notice
    ADD CONSTRAINT notice_upms_notice_type_check
        CHECK (upms_notice_type IN ('TEAM_MATCHING', 'PROJECT_SETTING'));
