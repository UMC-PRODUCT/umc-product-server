-- INFRA_PLUS를 정식 커리큘럼 트랙으로 개방한다.
-- 커리큘럼(curriculum.track)과 챌린저 코드(challenger_record.tracks)의 허용 트랙 집합에 INFRA_PLUS를 추가한다.
-- challenger.tracks 는 이미 INFRA_PLUS 를 허용하고, study_group 은 이번 범위에서 제외한다.

-- 1. curriculum.track: INFRA_PLUS 커리큘럼 허용
ALTER TABLE public.curriculum DROP CONSTRAINT curriculum_track_check;
ALTER TABLE public.curriculum
    ADD CONSTRAINT curriculum_track_check
        CHECK (track IN ('PLAN', 'DESIGN', 'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER', 'INFRA_PLUS'));

-- 2. challenger_record.tracks: 허용 값 집합에 INFRA_PLUS 추가
ALTER TABLE public.challenger_record DROP CONSTRAINT challenger_record_tracks_values_check;
ALTER TABLE public.challenger_record
    ADD CONSTRAINT challenger_record_tracks_values_check
        CHECK (tracks <@ ARRAY['PLAN', 'DESIGN', 'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER', 'INFRA_PLUS']::TEXT[]);

-- 3. challenger_record.tracks: 중복 방지 검사에 INFRA_PLUS 포함
ALTER TABLE public.challenger_record DROP CONSTRAINT challenger_record_tracks_no_duplicates_check;
ALTER TABLE public.challenger_record
    ADD CONSTRAINT challenger_record_tracks_no_duplicates_check
        CHECK (
            cardinality(array_positions(tracks, 'PLAN')) <= 1
            AND cardinality(array_positions(tracks, 'DESIGN')) <= 1
            AND cardinality(array_positions(tracks, 'WEB_PRODUCT_ENGINEER')) <= 1
            AND cardinality(array_positions(tracks, 'MOBILE_PRODUCT_ENGINEER')) <= 1
            AND cardinality(array_positions(tracks, 'INFRA_PLUS')) <= 1
        );
