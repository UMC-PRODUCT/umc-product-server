-- 사용자 당 FCM Token을 여러 개 가지고 있을 수 있도록 UK를 제거합니다.
-- DB단에서 우선 제거하는 것이며, 추후 Service/Entity 단에서도 제거 예정입니다.

ALTER TABLE fcm_token
    DROP CONSTRAINT fcm_token_member_id_key;
