ALTER TABLE question
    ADD COLUMN parent_question_id  BIGINT  NULL REFERENCES question (id),
    ADD COLUMN is_active           BOOLEAN NOT NULL DEFAULT TRUE;
