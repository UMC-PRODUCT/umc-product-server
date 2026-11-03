package com.umc.product.member.application.port.in.query;

import com.umc.product.member.application.port.in.query.dto.MemberProfileInfo;

public interface GetMemberProfileUseCase {

    MemberProfileInfo getMemberProfileById(Long memberId);

    /**
     * 다른 회원에게 보여줄 프로필을 조회합니다. 비공개로 설정한 링크는 null입니다.
     */
    MemberProfileInfo getPublicMemberProfileById(Long memberId);
}
