package com.umc.product.member.application.port.in.query;

import java.util.Map;
import java.util.Set;

public interface GetMemberUseCase {
    MemberInfo getById(Long memberId);

    MemberProfileInfo getProfile(Long memberId);

    Map<Long, MemberProfileInfo> getProfiles(Set<Long> memberIds);

    boolean existsById(Long memberId);

    boolean existsByEmail(String email);
}
