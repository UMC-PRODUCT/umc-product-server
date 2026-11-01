-- 박박지현이 현재 FCM Token Entity에 아키텍쳐를 위반한 상태로 Member를 직접 참조 하고 있어서
-- 회원 탈퇴가 불가능한 문제가 발생하여 임시로 FK 제약을 완화함. 추후 조건 아예 삭제 필요함.

-- 기존 UNIQUE 제약 삭제 후 NULL 허용하는 UNIQUE로 재생성
-- (PostgreSQL UNIQUE는 기본적으로 NULL을 허용하므로 그대로 둬도 됨)

ALTER TABLE public.fcm_token
    ALTER COLUMN member_id DROP NOT NULL;

-- 기존 FK 제약 삭제
ALTER TABLE public.fcm_token
DROP
CONSTRAINT fk_fcm_token_member_id;

-- ON DELETE SET NULL로 FK 재생성
ALTER TABLE public.fcm_token
    ADD CONSTRAINT fk_fcm_token_member_id
        FOREIGN KEY (member_id)
            REFERENCES public.member (id)
            ON DELETE SET NULL;
