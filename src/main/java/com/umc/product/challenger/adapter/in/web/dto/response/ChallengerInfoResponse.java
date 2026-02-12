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
    /**
     * ChallengerInfo로부터 ChallengerInfoResponse 생성
     * <p>
     * 상위 레이어(Response)에서 하위 레이어(Info) DTO를 사용하여 변환
     * TODO: Member 정보 조회를 위해 MemberInfo 파라미터 추가 필요
     */
    @Deprecated
    public static ChallengerInfoResponse from(ChallengerInfo info) {
        log.error("ChallengerInfoResponse 생성 시 Member 정보를 포함하지 않는 메소드를 사용중입니다.");

        return new ChallengerInfoResponse(
            info.challengerId(),
            info.memberId(),
            info.gisuId(),
            info.part(),
            info.challengerPoints(),
            // Member 정보 (TODO: 실제 조회 필요)
            null, // name
            null, // nickname
            null, // email
            null, // schoolId
            null, // schoolName
            null, // profileImageLink
            null  // status
        );
    }

    @Deprecated
    public static ChallengerInfoResponse from(ChallengerInfo info, MemberInfo memberInfo) {
        log.error("잘못된 기수 정보를 전달하고 있습니다.");

        return new ChallengerInfoResponse(
            info.challengerId(),
            info.memberId(),
            info.gisuId(),
            info.part(),
            info.challengerPoints(),
            // Member 정보
            memberInfo.name(),
            memberInfo.nickname(),
            memberInfo.email(),
            memberInfo.schoolId(),
            memberInfo.schoolName(),
            memberInfo.profileImageLink(),
            memberInfo.status()
        );
    }

    public static ChallengerInfoResponse from(ChallengerInfo info, MemberInfo memberInfo, GisuInfo gisuInfo) {
        return ChallengerInfoResponse.builder()
            .challengerId(info.challengerId())
            .memberId(info.memberId())
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
