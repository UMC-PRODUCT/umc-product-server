-- 이관하면서 schedule 테이블 내에 새로 생긴 컬럼에 기본 값 넣기

UPDATE public.schedule
SET early_check_in_minutes   = 10,
    attendance_grace_minutes = 10,
    late_tolerance_minutes   = 20;
