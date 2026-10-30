package com.umc.product.member.application.port.out;

public interface DeleteMemberBlockPort {

    void delete(Long blockerMemberId, Long blockedMemberId);

    /**
     * 회원 탈퇴 시 그 회원이 blocker/blocked 어느 쪽이든 걸려 있는 차단 행을 모두 지운다.
     * member_block은 member에 FK가 없어 탈퇴만으로는 정리되지 않으므로 호출부가 직접 맞춰줘야 한다.
     */
    void deleteAllInvolvingMember(Long memberId);
}
