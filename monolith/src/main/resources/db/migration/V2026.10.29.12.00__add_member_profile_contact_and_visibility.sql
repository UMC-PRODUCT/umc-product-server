-- 명함에 노출할 연락처(이메일)와 외부 링크의 항목별 공개 여부를 저장합니다.
-- 연락처 이메일은 로그인 이메일(member.email)과 별개인 선택 입력값입니다.
-- 초기 정책상 모든 항목이 공개이므로 기존 프로필도 DEFAULT TRUE로 채웁니다.
ALTER TABLE member_profile
    ADD COLUMN contact_email        VARCHAR(100),
    ADD COLUMN contact_email_public BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN linked_in_public     BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN instagram_public     BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN github_public        BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN blog_public          BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN personal_public      BOOLEAN NOT NULL DEFAULT TRUE;
