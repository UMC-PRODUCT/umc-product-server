package com.umc.product.member.application.port.in.command;

public interface DeleteWithdrawnMemberIdentityUseCase {

    void lockByFormerMemberId(Long memberId);

    void deleteByFormerMemberId(Long memberId);
}
