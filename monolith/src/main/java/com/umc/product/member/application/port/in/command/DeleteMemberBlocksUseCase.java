package com.umc.product.member.application.port.in.command;

public interface DeleteMemberBlocksUseCase {

    /**
     * 회원 탈퇴 시 그 회원이 blocker/blocked 어느 쪽이든 걸려 있는 차단 행을 모두 지운다.
     */
    void deleteAllInvolving(Long memberId);
}
