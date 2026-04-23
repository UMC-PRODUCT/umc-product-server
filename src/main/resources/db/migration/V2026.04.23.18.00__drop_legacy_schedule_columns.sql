-- schedule 테이블의 더 이상 사용하지 않는 컬럼 삭제

ALTER TABLE public.schedule DROP COLUMN is_all_day;
ALTER TABLE public.schedule DROP COLUMN author_challenger_id;
ALTER TABLE public.schedule DROP COLUMN study_group_id;
