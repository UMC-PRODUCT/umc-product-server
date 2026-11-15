package com.umc.product.member.application.port.out;

import java.util.Optional;

import com.umc.product.member.application.port.out.dto.GraduatedNicknameReservation;

public interface LoadNicknameReservationPort {
    boolean isReserved(Long schoolId, String nickname);

    boolean isReclaimableBy(Long schoolId, String nickname, String ownerKey);

    Optional<GraduatedNicknameReservation> findGraduatedByOwner(String ownerKey);
}
