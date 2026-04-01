-- review 테이블에 동일 리뷰어 중복 리뷰 방지 제약 추가

ALTER TABLE review
    ADD CONSTRAINT uk_review_submission_reviewer UNIQUE (submission_id, reviewer_challenger_id);
