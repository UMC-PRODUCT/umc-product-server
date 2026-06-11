package com.umc.product.member.application.port.in.query.dto;

import java.util.List;
import java.util.Map;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

/**
 * /api/v2/member/me 응답에 사용되는 BFF Info DTO 입니다. 어댑터의 Response 매핑은 단순 위임만 합니다.
 */
public record MemberSummaryV2Info(
    MemberInfo member,
    MemberProfileInfo profile,
    boolean hasLocalCredential,
    long totalActivityDays,
    CurrentGisuMembership currentGisuMembership,
    List<ChallengerHistoryItem> challengerHistory
) {

    /**
     * 현재 활성 기수에 한정된 멤버십 스냅샷.
     * <p>
     * - 휴지기에는 null
     * - challenger는 활성 기수에 ACTIVE 챌린저 레코드가 있는 경우에만 set
     * - isAdmin은 활성 기수의 challengerRole 레코드 존재 여부 (status 무관)
     */
    public record CurrentGisuMembership(
        Long gisuId,
        Long generation,
        ActiveChallenger challenger,
        boolean isAdmin,
        List<ChallengerRoleType> roleTypes
    ) {
    }

    public record ActiveChallenger(
        Long challengerId,
        ChallengerPart part,
        ChallengerStatus challengerStatus,
        List<ChallengerPointInfo> points,
        Double totalPoints
    ) {
    }

    public record ChallengerHistoryItem(
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
        public static ChallengerHistoryItem of(
            ChallengerInfo challenger,
            GisuInfo gisu,
            ChapterInfo chapter,
            Map<Long, List<ChallengerRoleType>> roleTypesByChallengerId
        ) {
            return new ChallengerHistoryItem(
                challenger.challengerId(),
                gisu != null ? gisu.gisuId() : challenger.gisuId(),
                gisu != null ? gisu.generation() : null,
                chapter != null ? chapter.id() : null,
                chapter != null ? chapter.name() : null,
                challenger.part(),
                challenger.challengerStatus(),
                challenger.challengerPoints() == null ? List.of() : challenger.challengerPoints(),
                challenger.totalPoints() == null ? 0.0 : challenger.totalPoints(),
                roleTypesByChallengerId.getOrDefault(challenger.challengerId(), List.of())
            );
        }
    }

}
