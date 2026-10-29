-- 신고 운영 채널(Discord) 전달 상태와 신고 시점의 대상 스냅샷을 저장합니다.
-- 이 기능 이전에 접수된 신고는 전달 대상이 아니므로 delivery_status를 NULL로 둡니다.
ALTER TABLE report
    ADD COLUMN delivery_status        VARCHAR(20),
    ADD COLUMN delivery_attempt_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN delivered_at           TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN target_author_id       BIGINT,
    ADD COLUMN target_content         TEXT,
    ADD COLUMN target_context         VARCHAR(255);

ALTER TABLE report
    ADD CONSTRAINT ck_report_delivery_status
        CHECK (delivery_status IS NULL OR delivery_status IN ('PENDING', 'SENT', 'FAILED'));

COMMENT ON COLUMN report.target_author_id IS '신고 대상 작성자 member.id (신고 시점 스냅샷)';
COMMENT ON COLUMN report.target_content IS '신고 대상 내용 (신고 시점 스냅샷)';
COMMENT ON COLUMN report.target_context IS '신고 대상 위치 설명 (예: 스레드 제목, 신고 시점 스냅샷)';
