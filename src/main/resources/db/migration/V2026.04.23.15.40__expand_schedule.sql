-- V2 Schedule 도메인 데이터 이관 전 schedule 테이블 변경

-- 새 컬럼 추가
ALTER TABLE public.schedule
    ADD COLUMN author_member_id bigint;
ALTER TABLE public.schedule
    ADD COLUMN early_check_in_minutes bigint;
ALTER TABLE public.schedule
    ADD COLUMN attendance_grace_minutes bigint;
ALTER TABLE public.schedule
    ADD COLUMN late_tolerance_minutes bigint;

-- 컬럼 길이 변경
ALTER TABLE public.schedule ALTER COLUMN name TYPE varchar(100);
ALTER TABLE public.schedule ALTER COLUMN location_name TYPE varchar(100);
