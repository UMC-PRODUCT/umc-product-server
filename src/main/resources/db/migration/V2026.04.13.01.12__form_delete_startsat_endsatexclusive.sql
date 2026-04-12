ALTER TABLE form
DROP COLUMN ends_at_exclusive;

ALTER TABLE form
DROP COLUMN starts_at;

ALTER TABLE question
DROP CONSTRAINT IF EXISTS question_type_check;
