ALTER TABLE public.term_consent
    ADD COLUMN term_id bigint;

ALTER TABLE public.term_consent_log
    ADD COLUMN term_id bigint;

-- 기존 동의 데이터는 term_type만 보유하고 있어 실제 동의 당시의 term row를 역추적할 수 없다.
-- 현재 배포 시점에는 약관 변경 이력이 없다는 전제하에, 같은 type의 최신/활성 약관 row로 term_id를 백필한다.
-- 이후 약관 변경 이력이 존재하는 환경에서 이 마이그레이션을 재사용하는 경우,
-- NOT NULL 및 unique index 적용 전에 미매핑/중복 데이터 검증이 선행되어야 한다.
WITH latest_term_by_type AS (
    SELECT DISTINCT ON (type)
        id,
        type
    FROM public.term
    ORDER BY type, active DESC, created_at DESC, id DESC
)
UPDATE public.term_consent consent
SET term_id = term.id
FROM latest_term_by_type term
WHERE consent.term_type = term.type
  AND consent.term_id IS NULL;

WITH latest_term_by_type AS (
    SELECT DISTINCT ON (type)
        id,
        type
    FROM public.term
    ORDER BY type, active DESC, created_at DESC, id DESC
)
UPDATE public.term_consent_log consent_log
SET term_id = term.id
FROM latest_term_by_type term
WHERE consent_log.term_type = term.type
  AND consent_log.term_id IS NULL;

ALTER TABLE public.term_consent
    ALTER COLUMN term_id SET NOT NULL;

ALTER TABLE public.term_consent_log
    ALTER COLUMN term_id SET NOT NULL;

ALTER TABLE public.term_consent
    ADD CONSTRAINT fk_term_consent_term
        FOREIGN KEY (term_id) REFERENCES public.term (id);

ALTER TABLE public.term_consent_log
    ADD CONSTRAINT fk_term_consent_log_term
        FOREIGN KEY (term_id) REFERENCES public.term (id);

CREATE UNIQUE INDEX ux_term_consent_member_term
    ON public.term_consent (member_id, term_id);

CREATE INDEX ix_term_consent_log_member_term_occurred
    ON public.term_consent_log (member_id, term_id, occurred_at DESC);
