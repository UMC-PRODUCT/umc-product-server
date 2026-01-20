package com.umc.product.challenger.adapter.in.web.dto.response;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.MemberStatus;
import java.util.List;

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
    public static ChallengerInfoResponse from(ChallengerInfo info) {
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

    /**
     * ChallengerInfo와 Member 정보로부터 ChallengerInfoResponse 생성
     * TODO: Member 도메인 구현 후 활성화
     */
    // public static ChallengerInfoResponse from(ChallengerInfo info, MemberInfo memberInfo, SchoolInfo schoolInfo) {
    //     return new ChallengerInfoResponse(
    //             info.challengerId(),
    //             info.memberId(),
    //             info.gisuId(),
    //             info.part(),
    //             info.challengerPoints(),
    //             // Member 정보
    //             memberInfo.name(),
    //             memberInfo.nickname(),
    //             memberInfo.email(),
    //             schoolInfo.id(),
    //             schoolInfo.name(),
    //             memberInfo.profileImageLink(),
    //             memberInfo.status()
    //     );
    // }
}
