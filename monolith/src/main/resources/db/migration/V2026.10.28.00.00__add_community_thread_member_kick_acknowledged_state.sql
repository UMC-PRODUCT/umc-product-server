-- 강퇴 미확인(KICKED)과 확인함(KICKED_ACKNOWLEDGED)을 구분하기 위해 state 값을 추가한다.
-- KICKED_ACKNOWLEDGED도 멤버십이 종료된 상태이므로 left_at NOT NULL 제약은 KICKED와 동일하게 유지한다.
ALTER TABLE community_thread_member
    DROP CONSTRAINT ck_community_thread_member_state;

ALTER TABLE community_thread_member
    ADD CONSTRAINT ck_community_thread_member_state
        CHECK (state IN ('ACTIVE', 'LEFT', 'KICKED', 'KICKED_ACKNOWLEDGED'));

ALTER TABLE community_thread_member
    DROP CONSTRAINT ck_community_thread_member_lifecycle;

ALTER TABLE community_thread_member
    ADD CONSTRAINT ck_community_thread_member_lifecycle
        CHECK (
            (state = 'ACTIVE' AND left_at IS NULL)
            OR (state IN ('LEFT', 'KICKED', 'KICKED_ACKNOWLEDGED') AND left_at IS NOT NULL)
        );
