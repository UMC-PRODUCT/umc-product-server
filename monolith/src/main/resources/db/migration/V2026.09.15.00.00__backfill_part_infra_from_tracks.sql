-- 11기의 단일 기본 Track을 같은 이름의 Part로 복사한다.
-- 기존 Track 컬럼과 리크루팅 데이터는 유지하고, 이전 기수의 학습 정보는 변경하지 않는다.
-- infra 컬럼은 현재 코드와의 호환을 위해 추가하지만, 이번 복사에서는 인프라 수강을 활성화하지 않는다.

-- 1. part CHECK에 신규 파트(WEB_PRODUCT_ENGINEER, MOBILE_PRODUCT_ENGINEER) 추가
ALTER TABLE public.challenger DROP CONSTRAINT challenger_part_check;
ALTER TABLE public.challenger ADD CONSTRAINT challenger_part_check
    CHECK ((part)::text = ANY (ARRAY[
        'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN',
        'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER'
    ]::text[]));

ALTER TABLE public.challenger_record DROP CONSTRAINT challenger_record_part_check;
ALTER TABLE public.challenger_record ADD CONSTRAINT challenger_record_part_check
    CHECK ((part)::text = ANY (ARRAY[
        'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN',
        'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER'
    ]::text[]));

ALTER TABLE public.curriculum DROP CONSTRAINT curriculum_part_check;
ALTER TABLE public.curriculum ADD CONSTRAINT curriculum_part_check
    CHECK ((part)::text = ANY (ARRAY[
        'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN',
        'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER'
    ]::text[]));

ALTER TABLE public.study_group DROP CONSTRAINT study_group_part_check;
ALTER TABLE public.study_group ADD CONSTRAINT study_group_part_check
    CHECK ((part)::text = ANY (ARRAY[
        'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN',
        'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER'
    ]::text[]));

-- 2. infra boolean 컬럼 추가 (기본 false)
ALTER TABLE public.challenger ADD COLUMN infra boolean NOT NULL DEFAULT false;
ALTER TABLE public.challenger_record ADD COLUMN infra boolean NOT NULL DEFAULT false;

-- 3. part/track XOR 제약 제거 (백필 동안 part와 track이 잠시 공존해야 함)
ALTER TABLE public.curriculum DROP CONSTRAINT curriculum_learning_selection_check;
ALTER TABLE public.study_group DROP CONSTRAINT study_group_learning_selection_check;

-- 4. 11기 학습 정보만 복사한다. 기수 ID와 기수 번호는 다를 수 있다.
DO $$
DECLARE
    target_gisu_id BIGINT;
BEGIN
    IF (SELECT COUNT(*) FROM public.gisu WHERE generation = 11) > 1 THEN
        RAISE EXCEPTION '11기 기수 데이터가 여러 개입니다. 중복 기수를 먼저 확인해주세요.';
    END IF;

    SELECT id INTO target_gisu_id FROM public.gisu WHERE generation = 11;

    -- 여러 트랙 중 하나를 임의로 선택하지 않는다. 비수강 운영진의 빈 배열은 보존한다.
    IF EXISTS (
        SELECT 1 FROM public.challenger
        WHERE gisu_id = target_gisu_id AND cardinality(tracks) > 0
            AND (cardinality(tracks) <> 1 OR NOT tracks <@ ARRAY[
                'PLAN', 'DESIGN', 'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER'
            ]::TEXT[])
    ) THEN
        RAISE EXCEPTION '11기 챌린저는 기본 트랙이 하나여야 Part로 복사할 수 있습니다.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM public.challenger_record
        WHERE gisu_id = target_gisu_id AND cardinality(tracks) > 0
            AND (cardinality(tracks) <> 1 OR NOT tracks <@ ARRAY[
                'PLAN', 'DESIGN', 'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER'
            ]::TEXT[])
    ) THEN
        RAISE EXCEPTION '11기 등록 코드는 기본 트랙이 하나여야 Part로 복사할 수 있습니다.';
    END IF;

    -- 운영진 코드의 Part는 담당 파트이기도 하므로 다른 수강 트랙으로 덮어쓰지 않는다.
    IF EXISTS (
        SELECT 1 FROM public.challenger_record
        WHERE gisu_id = target_gisu_id AND cardinality(tracks) = 1
            AND challenger_role_type IS NOT NULL AND part IS NOT NULL
            AND part IS DISTINCT FROM (SELECT unnest(tracks))
    ) THEN
        RAISE EXCEPTION '11기 운영진 등록 코드의 담당 Part와 수강 Track이 다릅니다. 먼저 확인해주세요.';
    END IF;

    UPDATE public.challenger
    SET part = (SELECT unnest(tracks))
    WHERE gisu_id = target_gisu_id AND cardinality(tracks) = 1;

    UPDATE public.challenger_record
    SET part = (SELECT unnest(tracks))
    WHERE gisu_id = target_gisu_id AND cardinality(tracks) = 1;

    UPDATE public.curriculum
    SET part = track
    WHERE gisu_id = target_gisu_id AND track IS NOT NULL;

    UPDATE public.study_group
    SET part = track
    WHERE gisu_id = target_gisu_id AND track IS NOT NULL;
END $$;
