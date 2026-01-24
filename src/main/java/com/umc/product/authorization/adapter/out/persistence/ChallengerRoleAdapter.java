package com.umc.product.authorization.adapter.out.persistence;

import com.umc.product.authorization.application.port.out.LoadChallengerRolePort;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ChallengerRole 정보를 조회하는 Adapter
 */
@Component
@RequiredArgsConstructor
public class ChallengerRoleAdapter implements LoadChallengerRolePort {

    private final EntityManager entityManager;

    @Override
    public List<ChallengerRoleType> findRolesByMemberId(Long memberId) {
        String jpql = """
                SELECT cr.challengerRoleType
                FROM ChallengerRole cr
                JOIN cr.challenger c
                WHERE c.memberId = :memberId
                """;

        return entityManager.createQuery(jpql, ChallengerRoleType.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    @Override
    public List<ChallengerRoleType> findRolesByMemberIdAndGisuId(Long memberId, Long gisuId) {
        String jpql = """
                SELECT cr.challengerRoleType
                FROM ChallengerRole cr
                JOIN cr.challenger c
                WHERE c.memberId = :memberId
                  AND cr.gisuId = :gisuId
                """;

        return entityManager.createQuery(jpql, ChallengerRoleType.class)
                .setParameter("memberId", memberId)
                .setParameter("gisuId", gisuId)
                .getResultList();
    }
}
