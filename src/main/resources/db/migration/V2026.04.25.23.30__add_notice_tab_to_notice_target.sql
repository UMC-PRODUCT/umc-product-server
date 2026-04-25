-- 공지 탭 구분 컬럼 추가
-- 기존 데이터는 target_staff_roles로 탭을 추정하여 채웁니다.
-- (신규 공지부터는 작성자 역할 기반으로 서비스에서 결정)
ALTER TABLE notice_target
    ADD COLUMN notice_tab VARCHAR(20) NOT NULL DEFAULT 'CHALLENGER';

