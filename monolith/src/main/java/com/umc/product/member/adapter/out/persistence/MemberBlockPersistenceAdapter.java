package com.umc.product.member.adapter.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.umc.product.member.application.port.out.DeleteMemberBlockPort;
import com.umc.product.member.application.port.out.LoadMemberBlockPort;
import com.umc.product.member.application.port.out.SaveMemberBlockPort;
import com.umc.product.member.domain.MemberBlock;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberBlockPersistenceAdapter implements LoadMemberBlockPort, SaveMemberBlockPort, DeleteMemberBlockPort {

    private static final String BLOCKER_BLOCKED_DUPLICATE_CONSTRAINT = "uq_member_block_blocker_blocked";

    private final MemberBlockJpaRepository memberBlockJpaRepository;

    @Override
    public Optional<MemberBlock> findByBlockerAndBlocked(Long blockerMemberId, Long blockedMemberId) {
        return memberBlockJpaRepository.findByBlockerMemberIdAndBlockedMemberId(blockerMemberId, blockedMemberId);
    }

    @Override
    public List<MemberBlock> listByBlockerMemberId(Long blockerMemberId) {
        return memberBlockJpaRepository.findAllByBlockerMemberId(blockerMemberId);
    }

    @Override
    public Map<Long, Set<Long>> listBlockedMemberIdsByBlockerIds(Set<Long> blockerMemberIds) {
        if (blockerMemberIds.isEmpty()) {
            return Map.of();
        }
        return memberBlockJpaRepository.findAllByBlockerMemberIdIn(blockerMemberIds).stream()
            .collect(Collectors.groupingBy(
                MemberBlock::getBlockerMemberId,
                Collectors.mapping(MemberBlock::getBlockedMemberId, Collectors.toSet())
            ));
    }

    @Override
    public MemberBlock save(MemberBlock memberBlock) {
        try {
            return memberBlockJpaRepository.saveAndFlush(memberBlock);
        } catch (DataIntegrityViolationException exception) {
            if (isBlockerBlockedDuplicate(exception)) {
                throw new MemberDomainException(MemberErrorCode.MEMBER_BLOCK_ALREADY_EXISTS, exception);
            }
            throw exception;
        }
    }

    @Override
    public void delete(Long blockerMemberId, Long blockedMemberId) {
        memberBlockJpaRepository.deleteByBlockerMemberIdAndBlockedMemberId(blockerMemberId, blockedMemberId);
    }

    private boolean isBlockerBlockedDuplicate(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException
                && BLOCKER_BLOCKED_DUPLICATE_CONSTRAINT.equals(constraintViolationException.getConstraintName())) {
                return true;
            }
            if (cause.getMessage() != null && cause.getMessage().contains(BLOCKER_BLOCKED_DUPLICATE_CONSTRAINT)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
