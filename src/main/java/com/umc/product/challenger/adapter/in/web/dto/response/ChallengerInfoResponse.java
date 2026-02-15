package com.umc.product.challenger.adapter.in.web.dto.response;

import com.umc.product.authorization.adapter.in.web.dto.response.ChallengerRoleResponse;
import com.umc.product.authorization.application.port.in.query.ChallengerRoleInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
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
    Long chapterId,
    String chapterName,
    ChallengerPart part,
    ChallengerStatus challengerStatus,
    // TODO: 호환성을 위해 유지하는 것으로, 다음 마이너 버전 업데이트에 제거하도록 함
    List<ChallengerPointInfo> challengerPoints,
    List<ChallengerPointInfo> points,
    Double totalPoints,
    List<ChallengerRoleResponse> roles,

    // 멤버 정보
    String name,
    String nickname,
    String email,
    Long schoolId,
    String schoolName,
    String profileImageLink,
    MemberStatus memberStatus,
    // TODO: 호환성을 위해 유지함, 추후 제거
    MemberStatus status
) {
    public static ChallengerInfoResponse from(
        ChallengerInfo info, MemberInfo memberInfo,
        GisuInfo gisuInfo, ChapterInfo chapterInfo) {
        return ChallengerInfoResponse.builder()
            .challengerId(info.challengerId())
            .memberId(info.memberId())
            // 기수
            .gisuId(gisuInfo.gisuId())
            .gisu(gisuInfo.generation())
            // 지부
            .chapterId(chapterInfo.id())
            .chapterName(chapterInfo.name())
            .part(info.part())
            .challengerPoints(info.challengerPoints())
            .points(info.challengerPoints())

            // Member 정보
            .name(memberInfo.name())
            .nickname(memberInfo.nickname())
            .email(null) // 이메일은 보안 상 제거하도록 함
            .schoolId(memberInfo.schoolId())
            .schoolName(memberInfo.schoolName())
            .profileImageLink(memberInfo.profileImageLink())
            .memberStatus(memberInfo.status())
            .status(memberInfo.status())
            .build();
    }

    /**
     * 챌린저 응답을 생성합니다.
     * <p>
     * TODO: assembler로의 분리도 고려해보아야 합니다.
     *
     * @param roles 주어지는 roles는 해당 챌린저의 기수에 한해야 합니다. assert로 검증합니다.
     */
    public static ChallengerInfoResponse from(
        ChallengerInfo info, MemberInfo memberInfo, GisuInfo gisuInfo, ChapterInfo chapterInfo,
        List<ChallengerRoleInfo> roles) {
        return ChallengerInfoResponse.builder()
            .challengerId(info.challengerId())
            .memberId(info.memberId())
            // 기수
            .gisuId(gisuInfo.gisuId())
            .gisu(gisuInfo.generation())
            // 지부
            .chapterId(chapterInfo.id())
            .chapterName(chapterInfo.name())
            // 파트
            .part(info.part())
            .challengerStatus(info.challengerStatus())
            // 상벌점
            .challengerPoints(info.challengerPoints())
            .points(info.challengerPoints())
            // 역할 (권한)
            .roles(roles.stream()
                .map(roleInfo -> {
                    // 보통은 이렇게 assert로 검증하기보다는, 서비스 레이어에서 미리 검증하는게 좋긴 합니다만...
                    assert roleInfo.gisuId().equals(gisuInfo.gisuId());
                    return ChallengerRoleResponse.from(roleInfo, gisuInfo);
                }).toList())

            // Member 정보
            .name(memberInfo.name())
            .nickname(memberInfo.nickname())
            .email(null) // 이메일은 보안 상 제거하도록 함
            .schoolId(memberInfo.schoolId())
            .schoolName(memberInfo.schoolName())
            .profileImageLink(memberInfo.profileImageLink())
            .memberStatus(memberInfo.status())
            .status(memberInfo.status())
            .build();
    }
}
