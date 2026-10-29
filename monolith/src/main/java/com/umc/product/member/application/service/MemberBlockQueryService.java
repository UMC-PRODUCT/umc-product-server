package com.umc.product.member.application.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.query.GetBlockedMemberIdsUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.ListBlockedMembersUseCase;
import com.umc.product.member.application.port.in.query.dto.BlockedMemberInfo;
import com.umc.product.member.application.port.out.LoadMemberBlockPort;
import com.umc.product.member.domain.MemberBlock;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberBlockQueryService implements ListBlockedMembersUseCase, GetBlockedMemberIdsUseCase {

    private final LoadMemberBlockPort loadMemberBlockPort;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public List<BlockedMemberInfo> listBlockedMembers(Long blockerMemberId) {
        List<MemberBlock> blocks = loadMemberBlockPort.listByBlockerMemberId(blockerMemberId);
        if (blocks.isEmpty()) {
            return List.of();
        }

        Set<Long> blockedMemberIds = blocks.stream()
            .map(MemberBlock::getBlockedMemberId)
            .collect(Collectors.toSet());
        Map<Long, String> names = getMemberUseCase.findAllNamesByIds(blockedMemberIds);

        return blocks.stream()
            .map(block -> new BlockedMemberInfo(
                block.getBlockedMemberId(),
                names.get(block.getBlockedMemberId()),
                block.getCreatedAt()
            ))
            .toList();
    }

    @Override
    public Set<Long> getBlockedMemberIds(Long blockerMemberId) {
        return loadMemberBlockPort.listByBlockerMemberId(blockerMemberId).stream()
            .map(MemberBlock::getBlockedMemberId)
            .collect(Collectors.toSet());
    }

    @Override
    public Map<Long, Set<Long>> getBlockedMemberIdsBatch(Set<Long> blockerMemberIds) {
        if (blockerMemberIds.isEmpty()) {
            return Map.of();
        }
        return loadMemberBlockPort.listBlockedMemberIdsByBlockerIds(blockerMemberIds);
    }
}
