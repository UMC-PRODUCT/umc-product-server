ALTER TABLE notice
    ADD COLUMN last_reminder_date DATE;

COMMENT ON COLUMN notice.last_reminder_date IS '마지막으로 전체 재알림을 요청한 한국 날짜';
