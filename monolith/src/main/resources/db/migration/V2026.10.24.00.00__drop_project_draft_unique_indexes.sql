-- 잠금 대기가 길어지면 실패시켜 배포 중 쓰기 요청이 계속 대기하지 않도록 한다.
SET LOCAL lock_timeout = '5s';

-- 기존 프로젝트는 보존하고 작성자·PM별 초안 1개 제한만 제거한다.
DROP INDEX IF EXISTS uk_project_creator_gisu_draft;
DROP INDEX IF EXISTS uk_project_owner_gisu_draft;

-- 초안 개수와 무관하게 작성자별 최신순 조회를 지원한다.
CREATE INDEX IF NOT EXISTS idx_project_creator_gisu_draft_created_at
    ON project (created_by_member_id, gisu_id, created_at DESC, id DESC)
    WHERE status = 'DRAFT';
