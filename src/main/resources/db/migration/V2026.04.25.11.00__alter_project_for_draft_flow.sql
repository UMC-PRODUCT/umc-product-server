-- Project 도메인 Phase 1 — DRAFT 플로우 + storage 도메인 정렬을 위한 schema 보정.
-- V2026.04.13.12.23__create_project_domain.sql 위에서 누락된 부분을 보강한다.

-- 1) DRAFT 단계의 빈 name 허용
ALTER TABLE project
    ALTER COLUMN name DROP NOT NULL;

-- 2) file_id 컬럼을 storage 도메인의 UUID 문자열과 정렬 (BIGINT → VARCHAR)
ALTER TABLE project
    ALTER COLUMN logo_file_id TYPE VARCHAR(36) USING logo_file_id::TEXT;

ALTER TABLE project
    ALTER COLUMN thumbnail_file_id TYPE VARCHAR(36) USING thumbnail_file_id::TEXT;

-- 3) "한 PM당 한 기수 1개" 정책 (상태 무관)
ALTER TABLE project
    ADD CONSTRAINT uk_project_owner_gisu UNIQUE (product_owner_member_id, gisu_id);

-- 4) FileCategory CHECK 제거 — Java enum @Enumerated(EnumType.STRING)이 이미 가드,
--    enum 추가마다 migration 동반 비용 회피
ALTER TABLE file_metadata
    DROP CONSTRAINT file_metadata_category_check;
