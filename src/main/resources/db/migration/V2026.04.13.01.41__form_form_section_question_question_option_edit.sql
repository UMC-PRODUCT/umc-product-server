-- form 테이블에서 title not null 설정
-- form_section 테이블에서 orderNo를 int -> bigint
-- form_section 테이블에서 type, target_key 삭제

-- question 테이블에서 question_text -> title 이름 변경
-- question 테이블에 description 컬럼 추가
-- question 테이블에서 orderNo를 int -> bigint

-- question_option 테이블에서 orderNo를 int -> bigint

ALTER TABLE form
    ALTER COLUMN title SET NOT NULL;

ALTER TABLE form_section
    ALTER COLUMN order_no TYPE BIGINT;

ALTER TABLE form_section
    DROP
        COLUMN type,
    DROP
        COLUMN target_key;

ALTER TABLE question
    RENAME COLUMN question_text TO title;

ALTER TABLE question
    ADD COLUMN description VARCHAR(1000);

ALTER TABLE question
    ALTER COLUMN order_no TYPE BIGINT;

ALTER TABLE question_option
    ALTER COLUMN order_no TYPE BIGINT;
