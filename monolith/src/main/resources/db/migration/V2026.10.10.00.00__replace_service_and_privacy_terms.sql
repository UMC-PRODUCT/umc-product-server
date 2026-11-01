-- 서비스 이용약관과 개인정보 처리방침 v2를 신규 약관 버전으로 등록한다.
UPDATE public.term
SET active = false,
    updated_at = CURRENT_TIMESTAMP
WHERE active = true
  AND type IN ('SERVICE', 'PRIVACY');

INSERT INTO public.term (active, required, created_at, updated_at, link, type)
VALUES (true,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        'https://makeus-challenge.notion.site/v2-26db57f4596b83fba9c901def1ec35fc',
        'PRIVACY'),
       (true,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        'https://makeus-challenge.notion.site/v2-05fb57f4596b83e9aa9c81ca54de347f',
        'SERVICE');
