ALTER TABLE public.challenger_record
    DROP CONSTRAINT challenger_record_optional_chapter_check,
    ADD CONSTRAINT challenger_record_optional_chapter_check
        CHECK (
            chapter_id IS NOT NULL
            OR (
                (
                    (
                        challenger_role_type IS NOT NULL
                        AND challenger_role_type IN (
                            'CENTRAL_PRESIDENT', 'CENTRAL_VICE_PRESIDENT',
                            'CENTRAL_OPERATING_TEAM_MEMBER', 'CENTRAL_EDUCATION_TEAM_MEMBER'
                        )
                    )
                    OR (challenger_role_type IS NULL AND part IS NULL)
                )
                AND cardinality(tracks) = 0
                AND track IS NULL
            )
        );
