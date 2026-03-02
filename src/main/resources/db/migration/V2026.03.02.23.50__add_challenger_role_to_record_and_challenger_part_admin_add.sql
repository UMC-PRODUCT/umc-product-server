ALTER TABLE public.challenger_record
    ADD COLUMN challenger_role_type character varying(30)
        CONSTRAINT challenger_record_challenger_role_type_check
            CHECK ((challenger_role_type)::text = ANY (ARRAY[
    ('SUPER_ADMIN':: character varying)::text,
    ('CENTRAL_PRESIDENT':: character varying)::text,
    ('CENTRAL_VICE_PRESIDENT':: character varying)::text,
    ('CENTRAL_OPERATING_TEAM_MEMBER':: character varying)::text,
    ('CENTRAL_EDUCATION_TEAM_MEMBER':: character varying)::text,
    ('CHAPTER_PRESIDENT':: character varying)::text,
    ('SCHOOL_PRESIDENT':: character varying)::text,
    ('SCHOOL_VICE_PRESIDENT':: character varying)::text,
    ('SCHOOL_PART_LEADER':: character varying)::text,
    ('SCHOOL_ETC_ADMIN':: character varying)::text
    ]));

ALTER TABLE public.challenger_record
    ADD COLUMN organization_id bigint;

-- ChallengerPart에 ADMIN 추가에 따른 CHECK 제약 업데이트

-- 1. application.selected_part
ALTER TABLE public.application DROP CONSTRAINT application_selected_part_check;
ALTER TABLE public.application
    ADD CONSTRAINT application_selected_part_check
        CHECK ((selected_part)::text = ANY (ARRAY[
    'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN'
    ]::text[]));

-- 2. challenger.part
ALTER TABLE public.challenger DROP CONSTRAINT challenger_part_check;
ALTER TABLE public.challenger
    ADD CONSTRAINT challenger_part_check
        CHECK ((part)::text = ANY (ARRAY[
    'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN'
    ]::text[]));

-- 3. challenger_record.part
ALTER TABLE public.challenger_record DROP CONSTRAINT challenger_record_part_check;
ALTER TABLE public.challenger_record
    ADD CONSTRAINT challenger_record_part_check
        CHECK ((part)::text = ANY (ARRAY[
    'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN'
    ]::text[]));

-- 4. challenger_role.responsible_part
ALTER TABLE public.challenger_role DROP CONSTRAINT challenger_role_responsible_part_check;
ALTER TABLE public.challenger_role
    ADD CONSTRAINT challenger_role_responsible_part_check
        CHECK ((responsible_part)::text = ANY (ARRAY[
    'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN'
    ]::text[]));

-- 5. curriculum.part
ALTER TABLE public.curriculum DROP CONSTRAINT curriculum_part_check;
ALTER TABLE public.curriculum
    ADD CONSTRAINT curriculum_part_check
        CHECK ((part)::text = ANY (ARRAY[
    'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN'
    ]::text[]));

-- 6. interview_question_sheet.part_key (COMMON 포함)
ALTER TABLE public.interview_question_sheet DROP CONSTRAINT interview_question_sheet_part_key_check;
ALTER TABLE public.interview_question_sheet
    ADD CONSTRAINT interview_question_sheet_part_key_check
        CHECK ((part_key)::text = ANY (ARRAY[
    'COMMON', 'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN'
    ]::text[]));

-- 7. recruitment_part.part
ALTER TABLE public.recruitment_part DROP CONSTRAINT recruitment_part_part_check;
ALTER TABLE public.recruitment_part
    ADD CONSTRAINT recruitment_part_part_check
        CHECK ((part)::text = ANY (ARRAY[
    'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN'
    ]::text[]));

-- 8. study_group.part
ALTER TABLE public.study_group DROP CONSTRAINT study_group_part_check;
ALTER TABLE public.study_group
    ADD CONSTRAINT study_group_part_check
        CHECK ((part)::text = ANY (ARRAY[
    'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN'
    ]::text[]));
