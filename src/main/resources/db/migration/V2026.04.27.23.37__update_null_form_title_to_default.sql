-- form.title 의 NULL 데이터에 기본값 주입
--
-- V2026.04.13.01.41 에서 form.title 에 NOT NULL 제약이 추가됐지만,
-- dev 환경에 NULL 행이 존재하여 운영 정합성 회복을 위해 정리합니다.

UPDATE form
SET title = '폼 제목',
    updated_at = now()
WHERE title IS NULL;
