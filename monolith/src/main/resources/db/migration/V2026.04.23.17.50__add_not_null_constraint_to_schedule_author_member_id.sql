--- schedule 테이블의 author_member_id에 NOT NULL 제약 추가

ALTER TABLE public.schedule
    ALTER COLUMN author_member_id SET NOT NULL;
