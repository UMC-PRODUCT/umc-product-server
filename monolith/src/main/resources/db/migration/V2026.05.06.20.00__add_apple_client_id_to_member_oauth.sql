-- Apple Sign-In은 플랫폼별로 다른 client_id(iOS Bundle ID vs Web Services ID)를 사용한다.
-- refresh token revoke 시 발급 당시와 동일한 client_id가 필요하므로 plain text로 보관한다.
ALTER TABLE member_oauth
    ADD COLUMN apple_client_id VARCHAR(255);

-- 기존 APPLE provider 행은 단일 client_id를 사용했던 시절이므로 'com.umc.product'로 일괄 backfill한다.
UPDATE member_oauth
SET apple_client_id = 'com.umc.product'
WHERE oauth_provider = 'APPLE';
