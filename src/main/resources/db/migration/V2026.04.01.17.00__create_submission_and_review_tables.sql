-- 1. submission 테이블 생성
CREATE TABLE submission
(
    id                      BIGSERIAL                   NOT NULL,
    challenger_workbook_id  BIGINT                      NOT NULL,
    content                 TEXT,
    created_at              TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at              TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_submission PRIMARY KEY (id),
    CONSTRAINT uk_submission_challenger_workbook_id UNIQUE (challenger_workbook_id),
    CONSTRAINT fk_submission_challenger_workbook
        FOREIGN KEY (challenger_workbook_id) REFERENCES challenger_workbook (id)
);

-- 2. review 테이블 생성
CREATE TABLE review
(
    id            BIGSERIAL                   NOT NULL,
    submission_id BIGINT                      NOT NULL,
    reviewer_challenger_id BIGINT             NOT NULL,
    feedback      TEXT,
    status        CHARACTER VARYING(255)      NOT NULL,
    best_reason   TEXT,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_review PRIMARY KEY (id),
    CONSTRAINT fk_review_submission
        FOREIGN KEY (submission_id) REFERENCES submission (id),
    CONSTRAINT review_status_check
        CHECK ((status)::TEXT = ANY
               ((ARRAY ['PASS'::CHARACTER VARYING, 'FAIL'::CHARACTER VARYING, 'BEST'::CHARACTER VARYING])::TEXT[]))
);

-- 3. 기존 데이터 마이그레이션
-- 3-1. PENDING이 아닌 워크북은 제출 이력이 있음
INSERT INTO submission (challenger_workbook_id, content, created_at, updated_at)
SELECT id, submission, created_at, updated_at
FROM challenger_workbook
WHERE status != 'PENDING';

-- 3-2. 심사 완료된 워크북(PASS, FAIL, BEST)의 리뷰 기록 마이그레이션
INSERT INTO review (submission_id, reviewer_challenger_id, feedback, status, best_reason, created_at, updated_at)
SELECT s.id, 0, cw.feedback, cw.status, cw.best_reason, cw.created_at, cw.updated_at
FROM challenger_workbook cw
JOIN submission s ON s.challenger_workbook_id = cw.id
WHERE cw.status IN ('PASS', 'FAIL', 'BEST');

-- 4. challenger_workbook에서 제출/심사 관련 컬럼 제거
ALTER TABLE challenger_workbook
    DROP COLUMN submission;
ALTER TABLE challenger_workbook
    DROP COLUMN feedback;
ALTER TABLE challenger_workbook
    DROP COLUMN best_reason;

-- 5. challenger_workbook unique constraint 제거 (같은 워크북 복수 배포 허용)
ALTER TABLE challenger_workbook
    DROP CONSTRAINT IF EXISTS uk_challenger_workbook_challenger_id_original_workbook_id;
