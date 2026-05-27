-- ProjectApplicationStatus 의 임시저장 상태명 변경: PENDING -> DRAFT
-- Survey FormResponse 의 DRAFT 상태와 의미적 일관성 확보 (#801)
-- @Enumerated(EnumType.STRING) 사용 -> DB에 enum 이름 그대로 저장되므로 row 값 정정 필요.

UPDATE project_application
SET status = 'DRAFT'
WHERE status = 'PENDING';
