package com.umc.product.member.application.port.in.query;

import java.util.Map;
import java.util.Set;

public interface GetMemberUseCase {
    MemberInfo getMemberInfoById(Long memberId);

    MemberProfileInfo getMemberProfileById(Long memberId);

    Map<Long, MemberInfo> getProfiles(Set<Long> memberIds);

    boolean existsById(Long memberId);

    boolean existsByEmail(String email);
}
