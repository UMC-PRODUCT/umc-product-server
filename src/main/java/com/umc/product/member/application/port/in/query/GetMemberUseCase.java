package com.umc.product.member.application.port.in.query;

public interface GetMemberUseCase {
    MemberInfo getById(Long memberId);

    MemberInfo getByEmail(String email);

    MemberProfileInfo getProfile(Long memberId);

    java.util.Map<Long, MemberProfileInfo> getProfiles(java.util.Set<Long> memberIds);

    boolean existsById(Long memberId);

    boolean existsByEmail(String email);
}
