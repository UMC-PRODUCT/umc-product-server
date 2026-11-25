UPDATE inquiry_category
SET active     = FALSE,
    updated_at = now()
WHERE name IN ('UMC 서비스', '기타');

INSERT INTO inquiry_category (name, display_order, active, created_at, updated_at)
VALUES ('출석·과제', 1, TRUE, now(), now()),
       ('계정·로그인', 2, TRUE, now(), now()),
       ('프로젝트·매칭', 3, TRUE, now(), now()),
       ('수료·활동 증명', 4, TRUE, now(), now())
ON CONFLICT (name) DO UPDATE SET display_order = EXCLUDED.display_order,
                                 active        = TRUE,
                                 updated_at    = now();
