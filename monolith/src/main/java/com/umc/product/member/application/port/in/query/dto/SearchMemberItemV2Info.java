package com.umc.product.member.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import java.util.List;

/**
 * /api/v2/member/search 응답의 항목 단위 정보입니다.
 * <p>
 * 회원 1명당 1개 항목이며, 같은 회원이 여러 기수 챌린저 이력을 가져도 별도 row로 분리되지 않습니다.
 * 대표 챌린저는 활성 기수 챌린저 우선, 없으면 가장 최신 기수의 챌린저로 선택됩니다.
 */
public record SearchMemberItemV2Info(
    Long memberId,
    String name,
    String nickname,
    String email,
    Long schoolId,
    String schoolName,
    String profileImageLink,
    PrimaryChallenger primaryChallenger,
    boolean isAdminInActiveGisu,
    List<Participation> participations
) {

    /**
     * 검색 결과 화면에 한 줄로 보여줄 대표 챌린저 정보.
     */
    public record PrimaryChallenger(
        Long challengerId,
        Long gisuId,
        Long generation,
        ChallengerPart part,
        ChallengerStatus challengerStatus
    ) {
    }

    /**
     * 회원이 보유한 챌린저 이력의 가벼운 요약. 검색 결과에서 회원 식별을 돕기 위해 함께 노출됩니다.
     */
    public record Participation(
        Long challengerId,
        Long gisuId,
        Long generation,
        ChallengerPart part,
        ChallengerStatus challengerStatus
    ) {
    }
}
