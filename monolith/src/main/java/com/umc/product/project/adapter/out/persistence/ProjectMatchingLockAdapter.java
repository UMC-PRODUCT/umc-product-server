package com.umc.product.project.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.umc.product.project.application.port.out.LockProjectMatchingPort;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectMatchingLockAdapter implements LockProjectMatchingPort {
    private final EntityManager entityManager;

    @Override
    public void lockByProjectId(Long projectId) {
        lock("(SELECT gisu_id FROM project WHERE id = :id)", projectId);
    }

    @Override
    public void lockByRoundId(Long roundId) {
        lock("(SELECT c.gisu_id FROM project_matching_round r JOIN chapter c ON c.id = r.chapter_id WHERE r.id = :id)",
            roundId);
    }

    @Override
    public void lockByGisuId(Long gisuId) {
        lock(":id", gisuId);
    }

    @Override
    public void lockByTransferId(Long transferId) {
        lock("(SELECT gisu_id FROM project_matching_chapter_transfer WHERE id = :id)", transferId);
    }

    private void lock(String gisuExpression, Long id) {
        // 트랜잭션 종료 시 자동 해제된다. 차수가 없는 기간에도 중복 배정을 막는다.
        entityManager.createNativeQuery("SELECT 1 FROM pg_advisory_xact_lock("
                + "hashtextextended('project-matching:' || CAST(" + gisuExpression + " AS text), 0))")
            .setParameter("id", id).getResultList();
    }
}
