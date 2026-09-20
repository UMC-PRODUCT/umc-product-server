-- ADR-004 §Decision 3: figma_comment_dispatch 의 domain_id FK를 ON DELETE RESTRICT 로 변경.
-- 도메인 삭제 → 재생성 시 발송 이력이 사라져 동일 댓글이 재발송되는 것을 방지한다.
-- 도메인 삭제 시 application 레이어에서 dispatch 이력 아카이브 후 삭제해야 한다.
ALTER TABLE figma_comment_dispatch
    DROP CONSTRAINT fk_fcd_domain;

ALTER TABLE figma_comment_dispatch
    ADD CONSTRAINT fk_fcd_domain
        FOREIGN KEY (domain_id)
            REFERENCES figma_routing_domain (id)
            ON DELETE RESTRICT;
