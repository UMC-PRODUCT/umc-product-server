package com.umc.product.member.application.port.in.query;

public interface GetMemberUseCase {
    MemberInfo getById(Long userId);

    MemberInfo getByEmail(String email);

    Boolean existsById(Long userId);

    Boolean existsByEmail(String email);
}
