-- 신고 1건에 신고 사유를 여러 개 담을 수 있도록 report_reason 테이블을 만듭니다.
-- sort_order는 신고자가 사유를 고른 순서(0부터)이고, 같은 신고에 같은 사유를 두 번 담을 수 없습니다.
CREATE TABLE report_reason
(
    report_id   BIGINT      NOT NULL,
    sort_order  INTEGER     NOT NULL,
    reason_code VARCHAR(32) NOT NULL,
    CONSTRAINT pk_report_reason PRIMARY KEY (report_id, sort_order),
    CONSTRAINT uk_report_reason_report_reason UNIQUE (report_id, reason_code),
    CONSTRAINT fk_report_reason_report
        FOREIGN KEY (report_id) REFERENCES report (id),
    CONSTRAINT ck_report_reason_sort_order
        CHECK (sort_order >= 0),
    CONSTRAINT ck_report_reason_reason_code
        CHECK (reason_code IN ('SPAM', 'ABUSE', 'INAPPROPRIATE', 'PRIVACY', 'HARASSMENT', 'IMPERSONATION', 'ETC'))
);

-- 기존 신고의 사유 1개를 첫 번째 사유로 옮깁니다.
INSERT INTO report_reason (report_id, sort_order, reason_code)
SELECT id, 0, reason_code
FROM report
WHERE reason_code IS NOT NULL;

COMMENT ON TABLE report_reason IS '신고에 담긴 신고 사유 목록 (선택한 순서대로)';
COMMENT ON COLUMN report_reason.sort_order IS '신고자가 사유를 고른 순서 (0부터)';
-- 배포 중 예전 서버가 이 컬럼만 읽고 쓰므로 지우지 않고 대표 사유로 계속 채웁니다. 제거는 별도 작업으로 합니다.
COMMENT ON COLUMN report.reason_code IS '대표 신고 사유 (첫 번째로 고른 사유). 전체 사유는 report_reason 참고';
