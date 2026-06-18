package com.umc.product.member.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerActivityPeriodUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ActivityPeriodSummary;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.in.query.GetMemberCredentialUseCase;
import com.umc.product.member.application.port.in.query.GetMemberProfileUseCase;
import com.umc.product.member.application.port.in.query.GetMemberSummaryV2UseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.in.query.dto.MemberProfileInfo;
import com.umc.product.member.application.port.in.query.dto.MemberSummaryV2Info;
import com.umc.product.member.application.port.in.query.dto.MemberSummaryV2Info.ActiveChallenger;
import com.umc.product.member.application.port.in.query.dto.MemberSummaryV2Info.ChallengerHistoryItem;
import com.umc.product.member.application.port.in.query.dto.MemberSummaryV2Info.CurrentGisuMembership;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

import lombok.RequiredArgsConstructor;

/**
 * /api/v2/member/me BFF UseCase 구현체.
 * <p>
 * 모든 외부 도메인 조회를 단일 readOnly 트랜잭션 안에서 수행해 응답 시점 일관성을 보장합니다.
 * 챌린저 수와 무관하게 외부 호출 수가 O(1)이 되도록 batch 로딩 패턴을 사용합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSummaryV2QueryService implements GetMemberSummaryV2UseCase {

    private final GetMemberUseCase getMemberUseCase;
    private final GetMemberCredentialUseCase getMemberCredentialUseCase;
    private final GetMemberProfileUseCase getMemberProfileUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetChallengerActivityPeriodUseCase getChallengerActivityPeriodUseCase;

    @Override
    public MemberSummaryV2Info getSummaryByMemberId(Long memberId) {
        MemberInfo memberInfo = getMemberUseCase.getById(memberId);
        boolean hasLocalCredential = getMemberCredentialUseCase.findCredentialByMemberId(memberId).isPresent();
        MemberProfileInfo profileInfo = getMemberProfileUseCase.getMemberProfileById(memberId);

        // 챌린저 + 점수 일괄 로딩 (D13에서 batch로 교체됨)
        List<ChallengerInfo> challengers = getChallengerUseCase.getAllByMemberId(memberId);

        Set<Long> gisuIds = challengers.stream()
            .map(ChallengerInfo::gisuId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Set<Long> challengerIds = challengers.stream()
            .map(ChallengerInfo::challengerId)
            .collect(Collectors.toSet());

        // 활성 기수: 휴지기에는 Optional.empty()
        Optional<GisuInfo> activeGisuOpt = getGisuUseCase.findActiveGisu();

        // 기수/지부/역할/활동일 일괄 조회
        Map<Long, GisuInfo> gisuByGisuId = gisuIds.isEmpty()
            ? Map.of()
            : getGisuUseCase.getByIds(gisuIds).stream()
                .collect(Collectors.toMap(GisuInfo::gisuId, g -> g));

        Map<Long, Map<Long, ChapterInfo>> chapterByGisuAndSchool = loadChapterMap(memberInfo, gisuIds);

        Map<Long, List<ChallengerRoleType>> roleTypesByChallengerId = challengerIds.isEmpty()
            ? Map.of()
            : getChallengerRoleUseCase.getAllRoleTypesByChallengerIds(challengerIds);

        ActivityPeriodSummary activitySummary =
            getChallengerActivityPeriodUseCase.calculateActivityPeriod(challengers, gisuByGisuId);

        // 현재 활성 기수 멤버십 조립
        CurrentGisuMembership currentMembership = activeGisuOpt
            .map(activeGisu -> buildCurrentMembership(activeGisu, challengers, roleTypesByChallengerId))
            .orElse(null);

        // 챌린저 이력 (기수 내림차순)
        List<ChallengerHistoryItem> history = challengers.stream()
            .sorted((a, b) -> {
                Long genA = Optional.ofNullable(gisuByGisuId.get(a.gisuId()))
                    .map(GisuInfo::generation).orElse(0L);
                Long genB = Optional.ofNullable(gisuByGisuId.get(b.gisuId()))
                    .map(GisuInfo::generation).orElse(0L);
                return Long.compare(genB, genA);
            })
            .map(c -> ChallengerHistoryItem.of(
                c,
                gisuByGisuId.get(c.gisuId()),
                getChapterInfo(chapterByGisuAndSchool, c.gisuId(), memberInfo.schoolId()),
                roleTypesByChallengerId
            ))
            .toList();

        return new MemberSummaryV2Info(
            memberInfo,
            profileInfo,
            hasLocalCredential,
            activitySummary.totalActivityDays(),
            currentMembership,
            history
        );
    }

    private Map<Long, Map<Long, ChapterInfo>> loadChapterMap(MemberInfo memberInfo, Set<Long> gisuIds) {
        if (gisuIds.isEmpty() || memberInfo.schoolId() == null) {
            return Map.of();
        }
        return getChapterUseCase.getChapterMapByGisuIdsAndSchoolIds(
            gisuIds,
            Set.of(memberInfo.schoolId())
        );
    }

    private ChapterInfo getChapterInfo(
        Map<Long, Map<Long, ChapterInfo>> chapterByGisuAndSchool,
        Long gisuId,
        Long schoolId
    ) {
        if (schoolId == null) {
            return null;
        }
        return chapterByGisuAndSchool.getOrDefault(gisuId, Map.of()).get(schoolId);
    }

    private CurrentGisuMembership buildCurrentMembership(
        GisuInfo activeGisu,
        List<ChallengerInfo> challengers,
        Map<Long, List<ChallengerRoleType>> roleTypesByChallengerId
    ) {
        Long activeGisuId = activeGisu.gisuId();

        // 활성 기수 챌린저 중 ACTIVE 상태인 레코드를 찾습니다 (D11).
        Optional<ChallengerInfo> activeChallengerOpt = challengers.stream()
            .filter(c -> Objects.equals(c.gisuId(), activeGisuId))
            .filter(c -> c.challengerStatus() == ChallengerStatus.ACTIVE)
            .findFirst();

        // 활성 기수에 속한 챌린저 행(상태 불문)의 역할 타입을 모두 모읍니다.
        List<ChallengerRoleType> roleTypes = challengers.stream()
            .filter(c -> Objects.equals(c.gisuId(), activeGisuId))
            .flatMap(c -> roleTypesByChallengerId.getOrDefault(c.challengerId(), List.of()).stream())
            .distinct()
            .sorted()
            .toList();

        ActiveChallenger activeChallenger = activeChallengerOpt
            .map(c -> new ActiveChallenger(
                c.challengerId(),
                c.part(),
                c.challengerStatus(),
                c.challengerPoints() == null ? Collections.emptyList() : c.challengerPoints(),
                c.totalPoints() == null ? 0.0 : c.totalPoints()
            ))
            .orElse(null);

        return new CurrentGisuMembership(
            activeGisu.gisuId(),
            activeGisu.generation(),
            activeChallenger,
            !roleTypes.isEmpty(),
            roleTypes
        );
    }
}
