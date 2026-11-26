package com.umc.product.member.application.port.out;

import java.util.List;

import com.umc.product.member.domain.MemberSystemRole;
import com.umc.product.member.domain.MemberSystemRoleType;

public interface LoadMemberSystemRolePort {

    List<MemberSystemRole> listByMemberId(Long memberId);

    List<MemberSystemRole> listByRoleType(MemberSystemRoleType roleType);
}
