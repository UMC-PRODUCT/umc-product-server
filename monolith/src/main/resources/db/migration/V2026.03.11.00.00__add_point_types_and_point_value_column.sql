-- challenger_point 테이블의 point(PointType) CHECK 제약조건 업데이트 및 point_value 컬럼 추가
-- 10기 변경사항: 새로운 상벌점 유형 추가 및 커스텀 포인트 값 지원

-- 1. 기존 CHECK 제약조건 삭제
ALTER TABLE public.challenger_point
DROP
CONSTRAINT challenger_point_point_check;

-- 2. 새로운 enum 값들을 포함하는 CHECK 제약조건 추가
ALTER TABLE public.challenger_point
    ADD CONSTRAINT challenger_point_point_check CHECK (
        (point)::text = ANY (
    (ARRAY [
    -- 기존 (10기 이전)
    'BEST_WORKBOOK',
    'WARNING',
    'OUT',

    -- 10기 이후 추가
    'CUSTOM',
    'BLOG_CHALLENGE',
    'BEST_WORKBOOK_V2',
    'UMC_EVENT_REVIEW',
    'PEER_REVIEW_SUBMISSION',
    'NO_WORKBOOK_MISSION',
    'STUDY_LATE',
    'STUDY_ABSENT',
    'EVENT_LATE',
    'EVENT_EARLY_LEAVE',
    'EVENT_LATE_CANCEL',
    'EVENT_NO_SHOW',
    'PART_LEAD_FEEDBACK_LATE',
    'SCHOOL_CORE_MEETING_ABSENT',
    'SCHOOL_CORE_TASK_NOT_COMPLETED'
    ])::text[]
    )
    );

-- 3. point_value 컬럼 추가 (CUSTOM 타입 등에서 enum 값 대신 사용할 커스텀 포인트 값)
ALTER TABLE public.challenger_point
    ADD COLUMN point_value integer;
