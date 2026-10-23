-- Survey 도메인 Answer.file_ids 의 컬럼 타입을 bigint[] -> text[] 로 변경.
--
-- 배경:
--   다른 모든 도메인 (Project, Member, Notice, Organization, Curriculum, Challenger 등) 이 storage 도메인의 fileId 를 String 으로 사용.
--   Survey Answer 만 Set<Long> 으로 정의돼 있어 타입 불일치 상태였음. 일관성 위해 String 으로 통일.
--
-- 데이터 안전성:
--   answer 테이블 자체는 PR #748 시점에 신설됐고, V2026.04.23.02.39 마이그레이션으로 legacy single_answer 의 투표 데이터 이관.
--   다만 이관 대상은 RADIO / DROPDOWN / CHECKBOX 객관식 답변뿐이며, 해당 INSERT 가 file_ids 컬럼을 채우지 않아 모든 row 의 file_ids 는 NULL.
--   FILE / PORTFOLIO 답변은 본 PR 에서 처음 지원하므로 file_ids 에 실제 값이 들어간 row 는 현재 시점 0건.
--   따라서 타입 변경 (bigint[] -> text[]) 은 NULL 값만 영향 — 데이터 손실 없이 안전.
ALTER TABLE answer
    ALTER COLUMN file_ids TYPE text[]
    USING file_ids::text[];
