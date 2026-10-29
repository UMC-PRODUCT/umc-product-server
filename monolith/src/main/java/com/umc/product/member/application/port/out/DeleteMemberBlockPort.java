package com.umc.product.member.application.port.out;

public interface DeleteMemberBlockPort {

    void delete(Long blockerMemberId, Long blockedMemberId);
}
