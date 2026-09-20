package com.umc.product.member.application.port.in.query;

import com.umc.product.member.application.port.in.query.dto.MemberProfileInfo;

public interface GetMemberProfileUseCase {

    MemberProfileInfo getMemberProfileById(Long memberId);
}
