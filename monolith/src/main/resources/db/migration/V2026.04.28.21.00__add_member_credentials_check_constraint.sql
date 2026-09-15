-- login_id와 password_hash는 항상 함께 존재하거나 함께 비어있어야 함
ALTER TABLE member
    ADD CONSTRAINT member_credentials_coexistence_check
        CHECK (
            (login_id IS NULL AND password_hash IS NULL) OR
            (login_id IS NOT NULL AND password_hash IS NOT NULL)
            );
