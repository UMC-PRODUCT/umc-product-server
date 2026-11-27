package com.umc.product.member.application.port.out;

import java.util.List;
import java.util.Set;

import com.umc.product.member.domain.MemberSystemRole;
import com.umc.product.member.domain.MemberSystemRoleType;

public interface LoadMemberSystemRolePort {

    List<MemberSystemRole> listByMemberId(Long memberId);

    Set<Long> listSuperAdminMemberIds();

    List<MemberSystemRole> listByRoleType(MemberSystemRoleType roleType);
}
