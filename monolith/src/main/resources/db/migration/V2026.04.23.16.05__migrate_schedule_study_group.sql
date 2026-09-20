-- study_group_schedule 테이블에 study_group_id, schedule_id 매핑

INSERT INTO public.study_group_schedule (study_group_id, schedule_id, created_at, updated_at)
SELECT study_group_id, id, created_at, updated_at
FROM public.schedule
WHERE study_group_id IS NOT NULL;
