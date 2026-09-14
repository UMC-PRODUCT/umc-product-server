-- 안녕하세요, recruitment 맘입니다. 제 손으로 제 자식들을 지우게 되었습니다. 슬프지만 앞으로 나아가야죠. 감사합니다.
-- 동국대학교 갈래 김민서 작성


-- survey 도메인 개편에 따른 recruitment 도메인 영향을 고려하지 않기 위하여 전체.. 삭제합니다................
DROP TABLE IF EXISTS interview_live_question CASCADE;
DROP TABLE IF EXISTS evaluation CASCADE;
DROP TABLE IF EXISTS application_part_preference CASCADE;
DROP TABLE IF EXISTS interview_assignment CASCADE;
DROP TABLE IF EXISTS interview_slot CASCADE;
DROP TABLE IF EXISTS interview_question_sheet CASCADE;
DROP TABLE IF EXISTS application CASCADE;
DROP TABLE IF EXISTS recruitment_schedule CASCADE;
DROP TABLE IF EXISTS recruitment_part CASCADE;
DROP TABLE IF EXISTS recruitment CASCADE;
