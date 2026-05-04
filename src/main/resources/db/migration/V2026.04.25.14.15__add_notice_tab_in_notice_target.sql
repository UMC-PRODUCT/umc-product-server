ALTER TABLE notice_target
    ADD COLUMN target_notice_tab text NOT NULL DEFAULT 'CHALLENGER';

ALTER TABLE notice_target
    ALTER COLUMN target_notice_tab DROP DEFAULT;
