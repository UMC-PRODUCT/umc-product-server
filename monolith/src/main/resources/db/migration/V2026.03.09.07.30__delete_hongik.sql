-- 홍익대학교 ID: 36
-- 홍익대학교 서울캠퍼스 ID: 37, 홍익대학교 세종캠퍼스 ID: 38

-- chapter_school 테이블에서 id가 26, 50, 72인 행이 적용받음 (seeding 데이터 기준)
-- chapter_school 테이블에서 school_id가 36인 행의 school_id를 37로 변경
UPDATE chapter_school
SET school_id = 37
WHERE school_id = 36;

-- challenger_record 테이블에서 school_id가 36인 행의 school_id를 37로 변경
UPDATE challenger_record
SET school_id = 37
WHERE school_id = 36;

-- school 테이블에서 id가 36인 행 삭제
DELETE
FROM school
WHERE id = 36;
