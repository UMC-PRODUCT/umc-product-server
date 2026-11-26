package com.umc.product.member.application.port.in.query;

import java.util.List;
import java.util.Set;

import com.umc.product.member.application.port.in.query.dto.MemberSystemRoleInfo;

public interface ListMemberSystemRoleUseCase {

    List<MemberSystemRoleInfo> listByMemberId(Long memberId);

    Set<Long> listSuperAdminMemberIds();

    /**
     * {@code UMC_PRODUCT} 시스템 역할을 직접 가진 회원만 돌려준다. {@code SUPER_ADMIN}만 가진 회원은 빠지므로 권한 판정에 쓰지 않는다.
     */
    List<Long> listUmcProductMemberIds();
}
