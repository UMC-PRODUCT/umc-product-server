package com.umc.product.member.application.service;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.command.DeleteWithdrawnMemberIdentityUseCase;
import com.umc.product.member.application.port.in.query.FindMemberIdsForReadmissionUseCase;
import com.umc.product.member.application.port.out.WithdrawnMemberIdentityPort;
import com.umc.product.member.domain.MemberRejoinKey;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberReadmissionIdentityService implements
    FindMemberIdsForReadmissionUseCase, DeleteWithdrawnMemberIdentityUseCase {

    private final WithdrawnMemberIdentityPort withdrawnMemberIdentityPort;

    @Override
    public Set<Long> findMemberIds(String email) {
        String normalizedEmail = MemberRejoinKey.normalizeVerifiedEmail(email);
        return withdrawnMemberIdentityPort.findMemberIds(normalizedEmail,
            MemberRejoinKey.fromVerifiedEmail(normalizedEmail));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void lockByFormerMemberId(Long memberId) {
        withdrawnMemberIdentityPort.lockByFormerMemberId(memberId);
    }

    @Override
    @Transactional
    public void deleteByFormerMemberId(Long memberId) {
        withdrawnMemberIdentityPort.deleteByFormerMemberId(memberId);
    }
}
