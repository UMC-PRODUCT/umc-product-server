package com.umc.product.member.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.out.LoadNicknameReservationPort;
import com.umc.product.member.application.port.out.LockSchoolNicknamePort;
import com.umc.product.member.application.port.out.ReserveNicknamePort;
import com.umc.product.member.application.port.out.dto.GraduatedNicknameReservation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NicknameReservationPersistenceAdapter implements
    LoadNicknameReservationPort, ReserveNicknamePort, LockSchoolNicknamePort {

    private final EntityManager entityManager;

    @Override
    public boolean isReserved(Long schoolId, String nickname) {
        return Boolean.TRUE.equals(entityManager.createNativeQuery("""
                SELECT EXISTS (SELECT 1 FROM member_nickname_reservation
                               WHERE school_id = :schoolId AND nickname = :nickname)
                """)
            .setParameter("schoolId", schoolId).setParameter("nickname", nickname).getSingleResult());
    }

    @Override
    public boolean isReclaimableBy(Long schoolId, String nickname, String ownerKey) {
        return Boolean.TRUE.equals(entityManager.createNativeQuery("""
                SELECT EXISTS (SELECT 1 FROM member_nickname_reservation
                               WHERE school_id = :schoolId
                                 AND nickname = :nickname
                                 AND owner_key = :ownerKey
                                 AND graduated = TRUE)
                """)
            .setParameter("schoolId", schoolId)
            .setParameter("nickname", nickname)
            .setParameter("ownerKey", ownerKey)
            .getSingleResult());
    }

    @Override
    public Optional<GraduatedNicknameReservation> findGraduatedByOwner(String ownerKey) {
        List<?> reservations = entityManager.createNativeQuery("""
                SELECT school_id, nickname FROM member_nickname_reservation
                WHERE owner_key = :ownerKey
                  AND graduated = TRUE
                ORDER BY created_at DESC
                FETCH FIRST 1 ROW ONLY
                """)
            .setParameter("ownerKey", ownerKey)
            .getResultList();
        return reservations.stream()
            .map(Object[].class::cast)
            .map(row -> new GraduatedNicknameReservation(((Number) row[0]).longValue(), (String) row[1]))
            .findFirst();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void reserve(Long schoolId, String nickname, String ownerKey, boolean graduated) {
        entityManager.createNativeQuery("""
                INSERT INTO member_nickname_reservation (school_id, nickname, owner_key, graduated)
                VALUES (:schoolId, :nickname, :ownerKey, :graduated)
                ON CONFLICT (school_id, nickname, owner_key)
                DO UPDATE SET graduated = member_nickname_reservation.graduated OR EXCLUDED.graduated
                """)
            .setParameter("schoolId", schoolId)
            .setParameter("nickname", nickname)
            .setParameter("ownerKey", ownerKey)
            .setParameter("graduated", graduated)
            .executeUpdate();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void lockSchool(Long schoolId) {
        entityManager.createNativeQuery("""
                SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(:scopeKey, 0))
                """)
            .setParameter("scopeKey", "member-nickname-school:" + schoolId).getSingleResult();
    }
}
