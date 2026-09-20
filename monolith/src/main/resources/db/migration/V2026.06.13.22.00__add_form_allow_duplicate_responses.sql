ALTER TABLE public.form
    ADD COLUMN allow_duplicate_responses BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE public.form f
SET allow_duplicate_responses = TRUE
WHERE EXISTS (
    SELECT 1
    FROM public.project_application_form paf
    WHERE paf.form_id = f.id
);
