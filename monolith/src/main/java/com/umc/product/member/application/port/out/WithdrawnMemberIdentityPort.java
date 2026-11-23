package com.umc.product.member.application.port.out;

import java.util.Set;

import com.umc.product.member.domain.WithdrawnMemberIdentity;

public interface WithdrawnMemberIdentityPort {

    void saveIfAbsent(WithdrawnMemberIdentity identity);

    Set<Long> findMemberIds(String normalizedEmail, String emailKey);

    void lockByFormerMemberId(Long memberId);

    void deleteByFormerMemberId(Long memberId);
}
