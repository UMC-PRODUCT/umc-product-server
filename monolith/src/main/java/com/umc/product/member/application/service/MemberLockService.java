package com.umc.product.member.application.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.command.LockMemberUseCase;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class MemberLockService implements LockMemberUseCase {

    private final LoadMemberPort loadMemberPort;

    @Override
    public void lockById(Long memberId) {
        loadMemberPort.findByIdForUpdate(memberId)
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    public void lockByIdIfExists(Long memberId) {
        loadMemberPort.findByIdForUpdate(memberId);
    }

    @Override
    public List<Long> lockExistingIdsForShare(Set<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return List.of();
        }
        return loadMemberPort.listIdsForShare(memberIds);
    }
}
