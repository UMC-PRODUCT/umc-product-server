package com.umc.product.member.application.port.in.query;

import com.umc.product.member.application.port.in.query.dto.MemberEmailHintInfo;

public interface GetMemberEmailHintUseCase {
    MemberEmailHintInfo getByIdentity(Long schoolId, String name, String nickname);
}
