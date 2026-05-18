package com.umc.product.member.adapter.in.web.v2.dto.response;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.in.query.dto.MemberProfileInfo;
import com.umc.product.member.application.port.in.query.dto.MemberSummaryV2Info;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * GET /api/v2/member/me 응답 DTO.
 * <p>
 * BFF 응답이며 한 호출로 프로필/활성 기수 멤버십/활동일/챌린저 이력을 모두 제공합니다.
 * v1 ChallengerInfoResponse의 호환 중복 필드(status/memberStatus, challengerPoints/points 등)는 v2에 도입하지 않습니다.
 */
public record MemberSummaryV2Response(
    Long id,

    String name,
    String nickname,

    String email,

    Long schoolId,
    String schoolName,
    String profileImageLink,
    MemberStatus status,
    MemberProfileInfo profile,

    @Schema(description = "챌린저로 활동한 총 일수")
    long totalActivityDays,
    @Schema(description = "현재 활성 기수에 대한 챌린저 정보", nullable = true)
    CurrentGisuMemberInfo currentGisuMemberInfo,
    @Schema(description = "모든 챌린저 기록 (최신 기수 우선)")
    List<ChallengerHistoryV2> challengerHistory
) {

    public static MemberSummaryV2Response from(MemberSummaryV2Info info) {
        MemberInfo m = info.member();
        MemberProfileInfo p = info.profile();

        return new MemberSummaryV2Response(
            m.id(),
            m.name(),
            m.nickname(),
            m.email(),
            m.schoolId(),
            m.schoolName(),
            m.profileImageLink(),
            m.status(),
            p,
            info.totalActivityDays(),
            info.currentGisuMembership() == null ? null : CurrentGisuMemberInfo.from(info.currentGisuMembership()),
            info.challengerHistory().stream().map(ChallengerHistoryV2::from).toList()
        );
    }

    public record CurrentGisuMemberInfo(
        Long gisuId,
        Long generation,

        @Schema(description = "활성 기수에서 현재 ACTIVE 상태인 챌린저 신분. ACTIVE가 아니면 null", nullable = true)
        ActiveChallenger challenger,

        @Schema(description = "활성 기수에 운영진 ChallengerRole이 하나라도 존재하는지")
        boolean isAdmin,
        @Schema(description = "활성 기수에 가지고 있는 역할 목록")
        List<ChallengerRoleType> roleTypes
    ) {
        public static CurrentGisuMemberInfo from(MemberSummaryV2Info.CurrentGisuMembership info) {
            return new CurrentGisuMemberInfo(
                info.gisuId(),
                info.generation(),
                info.challenger() == null ? null : ActiveChallenger.from(info.challenger()),
                info.isAdmin(),
                info.roleTypes()
            );
        }
    }

    public record ActiveChallenger(
        Long challengerId,
        ChallengerPart part,
        ChallengerStatus challengerStatus,
        List<ChallengerPointInfo> points,
        Double totalPoints
    ) {
        public static ActiveChallenger from(MemberSummaryV2Info.ActiveChallenger info) {
            return new ActiveChallenger(
                info.challengerId(),
                info.part(),
                info.challengerStatus(),
                info.points(),
                info.totalPoints()
            );
        }
    }

    public record ChallengerHistoryV2(
        Long challengerId,
        Long gisuId,
        Long generation,

        Long chapterId,
        String chapterName,

        ChallengerPart part,
        ChallengerStatus challengerStatus,
        List<ChallengerPointInfo> points,

        Double totalPoints,
        List<ChallengerRoleType> roleTypes
    ) {
        public static ChallengerHistoryV2 from(MemberSummaryV2Info.ChallengerHistoryItem info) {
            return new ChallengerHistoryV2(
                info.challengerId(),
                info.gisuId(),
                info.generation(),
                info.chapterId(),
                info.chapterName(),
                info.part(),
                info.challengerStatus(),
                info.points(),
                info.totalPoints(),
                info.roleTypes()
            );
        }
    }
}
