ALTER TABLE public.member
    ADD COLUMN role_type character varying(20);

UPDATE public.member
SET role_type = 'NORMAL'
WHERE role_type IS NULL;

UPDATE public.member m
SET role_type = 'ADMIN'
WHERE EXISTS (
    SELECT 1
    FROM public.challenger_role cr
             JOIN public.challenger c ON c.id = cr.challenger_id
    WHERE c.member_id = m.id
      AND cr.role_type = 'SUPER_ADMIN'
);

ALTER TABLE public.member
    ALTER COLUMN role_type SET DEFAULT 'NORMAL';

ALTER TABLE public.member
    ALTER COLUMN role_type SET NOT NULL;

ALTER TABLE public.member
    ADD CONSTRAINT member_role_type_check
        CHECK ((role_type)::text = ANY (ARRAY[
            ('NORMAL'::character varying)::text,
            ('ADMIN'::character varying)::text
        ]));

DELETE
FROM public.challenger_role
WHERE role_type = 'SUPER_ADMIN';

UPDATE public.challenger_record
SET challenger_role_type = NULL,
    organization_id = NULL
WHERE challenger_role_type = 'SUPER_ADMIN';

ALTER TABLE public.challenger_role
    DROP CONSTRAINT IF EXISTS challenger_role_role_type_check;

ALTER TABLE public.challenger_role
    ADD CONSTRAINT challenger_role_role_type_check
        CHECK ((role_type)::text = ANY (ARRAY[
            ('CENTRAL_PRESIDENT'::character varying)::text,
            ('CENTRAL_VICE_PRESIDENT'::character varying)::text,
            ('CENTRAL_OPERATING_TEAM_MEMBER'::character varying)::text,
            ('CENTRAL_EDUCATION_TEAM_MEMBER'::character varying)::text,
            ('CHAPTER_PRESIDENT'::character varying)::text,
            ('SCHOOL_PRESIDENT'::character varying)::text,
            ('SCHOOL_VICE_PRESIDENT'::character varying)::text,
            ('SCHOOL_PART_LEADER'::character varying)::text,
            ('SCHOOL_ETC_ADMIN'::character varying)::text
        ]));

ALTER TABLE public.challenger_record
    DROP CONSTRAINT IF EXISTS challenger_record_challenger_role_type_check;

ALTER TABLE public.challenger_record
    ADD CONSTRAINT challenger_record_challenger_role_type_check
        CHECK ((challenger_role_type)::text = ANY (ARRAY[
            ('CENTRAL_PRESIDENT'::character varying)::text,
            ('CENTRAL_VICE_PRESIDENT'::character varying)::text,
            ('CENTRAL_OPERATING_TEAM_MEMBER'::character varying)::text,
            ('CENTRAL_EDUCATION_TEAM_MEMBER'::character varying)::text,
            ('CHAPTER_PRESIDENT'::character varying)::text,
            ('SCHOOL_PRESIDENT'::character varying)::text,
            ('SCHOOL_VICE_PRESIDENT'::character varying)::text,
            ('SCHOOL_PART_LEADER'::character varying)::text,
            ('SCHOOL_ETC_ADMIN'::character varying)::text
        ]));
