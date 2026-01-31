package com.umc.product.member.application.port.in.query;

public interface GetMemberUseCase {
    MemberInfo getById(Long memberId);

    MemberInfo getByEmail(String email);

    MemberInfo getProfile(Long memberId);

    boolean existsById(Long memberId);

    boolean existsByEmail(String email);
}
