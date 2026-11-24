package com.umc.product.member.application.port.out;

import java.util.List;
import java.util.Set;

import com.umc.product.member.domain.MemberSystemRole;

public interface LoadMemberSystemRolePort {

    List<MemberSystemRole> listByMemberId(Long memberId);

    Set<Long> listSuperAdminMemberIds();
}
