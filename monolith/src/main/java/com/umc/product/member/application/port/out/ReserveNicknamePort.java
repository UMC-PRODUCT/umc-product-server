package com.umc.product.member.application.port.out;

public interface ReserveNicknamePort {
    void reserve(Long schoolId, String nickname, String ownerKey, boolean graduated);
}
