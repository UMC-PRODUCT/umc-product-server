package com.umc.product.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.command.BlockMemberUseCase;
import com.umc.product.member.application.port.in.command.UnblockMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.MemberBlockCommand;
import com.umc.product.member.application.port.in.command.dto.MemberBlockInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.out.DeleteMemberBlockPort;
import com.umc.product.member.application.port.out.LoadMemberBlockPort;
import com.umc.product.member.application.port.out.SaveMemberBlockPort;
import com.umc.product.member.domain.MemberBlock;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberBlockCommandService implements BlockMemberUseCase, UnblockMemberUseCase {

    private final GetMemberUseCase getMemberUseCase;
    private final LoadMemberBlockPort loadMemberBlockPort;
    private final SaveMemberBlockPort saveMemberBlockPort;
    private final DeleteMemberBlockPort deleteMemberBlockPort;

    @Override
    public MemberBlockInfo block(MemberBlockCommand command) {
        if (command.blockerMemberId().equals(command.blockedMemberId())) {
            throw new MemberDomainException(MemberErrorCode.MEMBER_BLOCK_SELF_NOT_ALLOWED);
        }
        if (!getMemberUseCase.existsById(command.blockedMemberId())) {
            throw new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        boolean alreadyBlocked = loadMemberBlockPort
            .findByBlockerAndBlocked(command.blockerMemberId(), command.blockedMemberId())
            .isPresent();
        if (alreadyBlocked) {
            throw new MemberDomainException(MemberErrorCode.MEMBER_BLOCK_ALREADY_EXISTS);
        }

        saveMemberBlockPort.save(MemberBlock.create(command.blockerMemberId(), command.blockedMemberId()));
        return new MemberBlockInfo(command.blockerMemberId(), command.blockedMemberId(), true);
    }

    @Override
    public MemberBlockInfo unblock(MemberBlockCommand command) {
        loadMemberBlockPort
            .findByBlockerAndBlocked(command.blockerMemberId(), command.blockedMemberId())
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_BLOCK_NOT_FOUND));

        deleteMemberBlockPort.delete(command.blockerMemberId(), command.blockedMemberId());
        return new MemberBlockInfo(command.blockerMemberId(), command.blockedMemberId(), false);
    }
}
