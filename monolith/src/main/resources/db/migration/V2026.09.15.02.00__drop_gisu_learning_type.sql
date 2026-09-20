-- 학습 기준을 Part로 통일했으므로 기수별 학습 방식 구분을 제거한다.
ALTER TABLE public.gisu DROP COLUMN learning_type;
