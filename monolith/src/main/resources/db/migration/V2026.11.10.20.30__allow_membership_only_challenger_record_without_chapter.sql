-- 이 기수에 참여하지 않는 학교의 프로덕트팀도 파트와 역할 없는 수강 없는 소속 코드를 받도록 지부 생략을 허용한다.
-- 기존 행은 모두 이전 제약을 통과했으므로 새 제약도 통과한다.
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
