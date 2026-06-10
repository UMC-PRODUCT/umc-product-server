package com.umc.product.member.application.port.in.query;

public interface GetMemberRoleUseCase {

    boolean isAdmin(Long memberId);
}
