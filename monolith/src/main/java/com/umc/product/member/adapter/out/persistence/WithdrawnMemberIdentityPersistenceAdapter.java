package com.umc.product.member.adapter.out.persistence;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.out.WithdrawnMemberIdentityPort;
import com.umc.product.member.domain.WithdrawnMemberIdentity;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WithdrawnMemberIdentityPersistenceAdapter implements WithdrawnMemberIdentityPort {

    private final EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveIfAbsent(WithdrawnMemberIdentity identity) {
        entityManager.createNativeQuery("""
                INSERT INTO withdrawn_member_identity (former_member_id, email_key, created_at, updated_at)
                VALUES (:memberId, :emailKey, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (email_key, former_member_id) DO NOTHING
                """)
            .setParameter("memberId", identity.getFormerMemberId())
            .setParameter("emailKey", identity.getEmailKey())
            .executeUpdate();
    }

    @Override
    public Set<Long> findMemberIds(String normalizedEmail, String emailKey) {
        List<?> memberIds = entityManager.createNativeQuery("""
                SELECT m.id
                FROM member m
                CROSS JOIN LATERAL (
                    SELECT btrim(m.email) AS email, split_part(btrim(m.email), '@', -1) AS domain
                ) normalized
                WHERE left(normalized.email, -length(normalized.domain)) || lower(normalized.domain) = :email
                UNION
                SELECT former_member_id
                FROM withdrawn_member_identity
                WHERE email_key = :emailKey
                """, Long.class)
            .setParameter("email", normalizedEmail)
            .setParameter("emailKey", emailKey)
            .getResultList();
        return memberIds.stream().map(Long.class::cast).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void lockByFormerMemberId(Long memberId) {
        entityManager.createNativeQuery("""
                SELECT id FROM withdrawn_member_identity
                WHERE former_member_id = :memberId
                ORDER BY id
                FOR UPDATE
                """)
            .setParameter("memberId", memberId)
            .getResultList();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteByFormerMemberId(Long memberId) {
        entityManager.createNativeQuery("""
                DELETE FROM withdrawn_member_identity WHERE former_member_id = :memberId
                """)
            .setParameter("memberId", memberId)
            .executeUpdate();
    }
}
