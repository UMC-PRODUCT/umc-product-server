package com.umc.product.challenger.adapter.in.web.dto.response;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import java.util.List;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public record ChallengerInfoResponse(
    Long challengerId,
    Long memberId,
    Long gisuId,
    Long gisu,
    ChallengerPart part,
    List<ChallengerPointInfo> challengerPoints,

    // 멤버 정보
    String name,
    String nickname,
    String email,
    Long schoolId,
    String schoolName,
    String profileImageLink,
    MemberStatus status
) {
    public static ChallengerInfoResponse from(ChallengerInfo info, MemberInfo memberInfo, GisuInfo gisuInfo) {
        return ChallengerInfoResponse.builder()
            .challengerId(info.challengerId())
            .memberId(info.memberId())
            .gisuId(gisuInfo.gisuId())
            .gisu(gisuInfo.generation())
            .part(info.part())
            .challengerPoints(info.challengerPoints())

            // Member 정보
            .name(memberInfo.name())
            .nickname(memberInfo.nickname())
            .email(null) // 이메일은 보안 상 제거하도록 함
            .schoolId(memberInfo.schoolId())
            .schoolName(memberInfo.schoolName())
            .profileImageLink(memberInfo.profileImageLink())
            .status(memberInfo.status())
            .build();
    }
}
