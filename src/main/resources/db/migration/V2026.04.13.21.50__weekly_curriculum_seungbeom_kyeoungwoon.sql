-- 각 주차별로 커리큘럼은 최대 두 개 (정규, 부록) 입니다.
-- 내부 워크북 수에 대한 제한은 없습니다!

ALTER TABLE weekly_curriculum
    ADD CONSTRAINT uk_weekly_curriculum_curriculum_id_week_no_extra UNIQUE (curriculum_id, week_no, is_extra);
