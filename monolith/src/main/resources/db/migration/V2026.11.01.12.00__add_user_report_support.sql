-- 사용자 신고를 지원하도록 신고 대상 종류와 사유, 처리 사유 제약을 넓힙니다.
-- 사용자 신고는 target_id에 대상 회원의 member.id를 저장하고, 신고자가 적은 상세 내용은 reason 컬럼(500자)에 저장합니다.
-- 사용자 신고는 스레드와 무관하므로 thread_id가 없고 reason_code는 필수입니다.
ALTER TABLE report
    DROP CONSTRAINT ck_report_target_type,
    DROP CONSTRAINT ck_report_reason_code,
    DROP CONSTRAINT ck_report_thread_message_fields;

ALTER TABLE report
    ADD CONSTRAINT ck_report_target_type
        CHECK (target_type IN ('POST', 'COMMENT', 'THREAD_MESSAGE', 'USER')),
    ADD CONSTRAINT ck_report_reason_code
        CHECK (reason_code IS NULL OR reason_code IN ('SPAM', 'ABUSE', 'INAPPROPRIATE', 'PRIVACY', 'HARASSMENT',
                                                      'IMPERSONATION', 'ETC')),
    ADD CONSTRAINT ck_report_thread_message_fields
        CHECK (
            (target_type = 'THREAD_MESSAGE'
                AND thread_id IS NOT NULL
                AND thread_id > 0
                AND reason_code IS NOT NULL)
            OR
            (target_type = 'USER' AND thread_id IS NULL AND reason_code IS NOT NULL)
            OR
            (target_type IN ('POST', 'COMMENT') AND thread_id IS NULL AND reason_code IS NULL)
        );

ALTER TABLE report_action
    DROP CONSTRAINT ck_report_action_reason_code;

ALTER TABLE report_action
    ADD CONSTRAINT ck_report_action_reason_code
        CHECK (reason_code IN ('SPAM', 'ABUSE', 'INAPPROPRIATE', 'PRIVACY', 'HARASSMENT', 'IMPERSONATION',
                               'OTHER_VIOLATION', 'NOT_VIOLATION', 'CONTENT_MISMATCH', 'UNVERIFIABLE', 'DUPLICATE',
                               'OTHER_REJECTION'));

COMMENT ON COLUMN report.reason IS '사용자 신고의 상세 내용 (최대 500자)';
