package com.umc.product.member.adapter.out.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.out.LoadNicknameReservationPort;
import com.umc.product.member.application.port.out.LockSchoolNicknamePort;
import com.umc.product.member.application.port.out.ReserveNicknamePort;

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
    @Transactional(propagation = Propagation.MANDATORY)
    public void reserve(Long schoolId, String nickname) {
        entityManager.createNativeQuery("""
                INSERT INTO member_nickname_reservation (school_id, nickname)
                VALUES (:schoolId, :nickname) ON CONFLICT (school_id, nickname) DO NOTHING
                """)
            .setParameter("schoolId", schoolId).setParameter("nickname", nickname).executeUpdate();
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
