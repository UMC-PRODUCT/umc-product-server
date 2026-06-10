ALTER TABLE challenger
    ADD CONSTRAINT uk_challenger_member_id_gisu_id
        UNIQUE (member_id, gisu_id);
