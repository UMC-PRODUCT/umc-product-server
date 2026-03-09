-- 이전 버전에서 홍익대학교를 삭제할 때 member 테이블은 변경하지 않은 부분을 수정

UPDATE member
SET school_id = 37
WHERE school_id = 36;
