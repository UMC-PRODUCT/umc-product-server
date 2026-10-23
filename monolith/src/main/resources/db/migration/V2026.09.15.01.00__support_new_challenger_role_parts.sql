ALTER TABLE public.challenger_role DROP CONSTRAINT challenger_role_responsible_part_check;
ALTER TABLE public.challenger_role ADD CONSTRAINT challenger_role_responsible_part_check
    CHECK ((responsible_part)::text = ANY (ARRAY[
        'PLAN', 'DESIGN', 'WEB', 'ANDROID', 'IOS', 'NODEJS', 'SPRINGBOOT', 'ADMIN',
        'WEB_PRODUCT_ENGINEER', 'MOBILE_PRODUCT_ENGINEER'
    ]::text[]));
