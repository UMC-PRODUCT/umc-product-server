-- schedule 테이블에 author_challenger_id -> author_member_id 매핑

UPDATE public.schedule s
SET author_member_id = c.member_id
FROM public.challenger c
WHERE s.author_challenger_id = c.id;
