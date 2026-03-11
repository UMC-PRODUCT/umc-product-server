-- challenger_workbook 테이블에 (challenger_id, original_workbook_id) 복합 유니크 제약조건 추가
-- 동일 챌린저가 같은 워크북을 중복 생성하는 것을 방지

ALTER TABLE public.challenger_workbook
    ADD CONSTRAINT uk_challenger_workbook_challenger_id_original_workbook_id
        UNIQUE (challenger_id, original_workbook_id);
