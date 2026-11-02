-- 프로덕트 문의 답변·관리 전용 시스템 역할 UMC_PRODUCT를 허용한다.
ALTER TABLE public.member_system_role
    DROP CONSTRAINT member_system_role_role_type_check;

ALTER TABLE public.member_system_role
    ADD CONSTRAINT member_system_role_role_type_check
        CHECK (role_type IN ('SUPER_ADMIN', 'UMC_PRODUCT'));
