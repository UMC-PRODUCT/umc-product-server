package com.umc.product.member.application.port.in.query;

import java.util.List;

import com.umc.product.member.application.port.in.query.dto.BlockedMemberInfo;

public interface ListBlockedMembersUseCase {

    List<BlockedMemberInfo> listBlockedMembers(Long blockerMemberId);
}
