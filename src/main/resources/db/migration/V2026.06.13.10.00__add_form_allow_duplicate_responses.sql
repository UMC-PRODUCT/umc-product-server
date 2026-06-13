ALTER TABLE public.form
    ADD COLUMN allow_duplicate_responses BOOLEAN NOT NULL DEFAULT FALSE;
