package com.umc.product.member.application.port.in.query;

public interface GetMemberUseCase {
    MemberInfo getById(Long userId);

    MemberInfo getByEmail(String email);

    boolean existsById(Long userId);

    boolean existsByEmail(String email);
}
