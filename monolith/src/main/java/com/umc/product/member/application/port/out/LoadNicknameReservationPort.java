package com.umc.product.member.application.port.out;

public interface LoadNicknameReservationPort {
    boolean isReserved(Long schoolId, String nickname);
}
