-- 학습 축을 트랙(tracks[]/track)에서 단일 part + infra boolean으로 되돌리는 1단계(확장·백필).
-- 이 단계에서는 track 컬럼을 유지하고 part/infra만 채운다. (track 컬럼 제거는 코드 전환 후 별도 마이그레이션)
-- 매핑은 항등: track 값과 part 값 문자열이 동일. INFRA_PLUS만 challenger/record는 infra=true, curriculum/study_group은 part=INFRA.

-- 1. part CHECK에 신규 파트(WEB_PRODUCT_ENGINEER, MOBILE_PRODUCT_ENGINEER, INFRA) 추가
ALTER TABLE public.challenger DROP CONSTRAINT challenger_part_check;
ALTER TABLE public.challenger ADD CONSTRAINT challenger_part_check
    CHECK ((part)::text = ANY (ARRAY[
        'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN',
        'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER', 'INFRA'
    ]::text[]));

ALTER TABLE public.challenger_record DROP CONSTRAINT challenger_record_part_check;
ALTER TABLE public.challenger_record ADD CONSTRAINT challenger_record_part_check
    CHECK ((part)::text = ANY (ARRAY[
        'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN',
        'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER', 'INFRA'
    ]::text[]));

ALTER TABLE public.curriculum DROP CONSTRAINT curriculum_part_check;
ALTER TABLE public.curriculum ADD CONSTRAINT curriculum_part_check
    CHECK ((part)::text = ANY (ARRAY[
        'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN',
        'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER', 'INFRA'
    ]::text[]));

ALTER TABLE public.study_group DROP CONSTRAINT study_group_part_check;
ALTER TABLE public.study_group ADD CONSTRAINT study_group_part_check
    CHECK ((part)::text = ANY (ARRAY[
        'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN',
        'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER', 'INFRA'
    ]::text[]));

-- 2. infra boolean 컬럼 추가 (기본 false)
ALTER TABLE public.challenger ADD COLUMN infra boolean NOT NULL DEFAULT false;
ALTER TABLE public.challenger_record ADD COLUMN infra boolean NOT NULL DEFAULT false;

-- 3. part/track XOR 제약 제거 (백필 동안 part와 track이 잠시 공존해야 함)
ALTER TABLE public.curriculum DROP CONSTRAINT curriculum_learning_selection_check;
ALTER TABLE public.study_group DROP CONSTRAINT study_group_learning_selection_check;

-- 4. 백필: tracks[]/track → part + infra
-- 4-1. challenger: 기본 트랙(INFRA_PLUS 제외) → part, INFRA_PLUS 포함 → infra=true
UPDATE public.challenger
SET infra = ('INFRA_PLUS' = ANY (tracks)),
    part = COALESCE(part, (SELECT t FROM unnest(tracks) AS t WHERE t <> 'INFRA_PLUS' LIMIT 1))
WHERE cardinality(tracks) > 0;

-- 4-2. challenger_record: 동일
UPDATE public.challenger_record
SET infra = ('INFRA_PLUS' = ANY (tracks)),
    part = COALESCE(part, (SELECT t FROM unnest(tracks) AS t WHERE t <> 'INFRA_PLUS' LIMIT 1))
WHERE cardinality(tracks) > 0;

-- 4-3. curriculum: track → part (INFRA_PLUS → INFRA)
UPDATE public.curriculum
SET part = CASE WHEN track = 'INFRA_PLUS' THEN 'INFRA' ELSE track END
WHERE track IS NOT NULL AND part IS NULL;

-- 4-4. study_group: 동일
UPDATE public.study_group
SET part = CASE WHEN track = 'INFRA_PLUS' THEN 'INFRA' ELSE track END
WHERE track IS NOT NULL AND part IS NULL;
