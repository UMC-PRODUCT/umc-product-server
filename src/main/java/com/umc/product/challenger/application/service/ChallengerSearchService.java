package com.umc.product.challenger.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerPointUseCase;
import com.umc.product.challenger.application.port.in.query.SearchChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.GlobalSearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.GlobalSearchChallengerItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerResult;
import com.umc.product.challenger.application.port.out.SearchChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerSearchService implements SearchChallengerUseCase {

    private final SearchChallengerPort searchChallengerPort;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetGisuUseCase getGisuUseCase;

    private final GetChallengerPointUseCase getChallengerPointUseCase;

    @Override
    public SearchChallengerResult offsetSearch(SearchChallengerQuery query, Pageable pageable) {
        // 페이지네이션을 적용해서 조건에 따라 챌린저 검색
        Page<Challenger> challengers = searchChallengerPort.search(query, pageable);

        // 검색된 챌린저의 파트별 인원 수 계산
        Map<ChallengerPart, Long> partCounts = buildPartCounts(query);
        // 챌린저별 상벌점 합계 계산
        Map<Long, Double> pointSums = buildPointSums(challengers);
        // 챌린저별 프로필 조회
        Map<Long, MemberInfo> memberProfiles = loadMemberProfiles(challengers);
        // 챌린저별 역할 조회
        Map<Long, List<ChallengerRoleType>> roleTypes = loadRoleTypes(challengers.getContent());
        // 챌린저별 기수 정보 조회
        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(challengers.getContent());

        // Page Item으로 변환
        Page<SearchChallengerItemInfo> items = challengers.map(challenger ->
            toItemInfo(challenger, memberProfiles, pointSums, roleTypes, gisuGenerationMap)
        );

        return new SearchChallengerResult(items, partCounts);
    }

    @Override
    public SearchChallengerCursorResult cursorSearch(SearchChallengerQuery query, Long cursor, int size) {
        // 조건에 맞는 챌린저 결과 및 파트별 인원 수 조회
        List<Challenger> challengers = searchChallengerPort.cursorSearch(query, cursor, size);
        Map<ChallengerPart, Long> partCounts = buildPartCounts(query);

        // cursor 기반 페이지네이션 처리
        // 조회된 결과가 요청한 size보다 많으면 다음 페이지가 존재하는 것으로 간주 및 마지막 값 자르기
        boolean hasNext = challengers.size() > size;
        List<Challenger> result = hasNext ? challengers.subList(0, size) : challengers;

        Map<Long, Double> pointSums = buildPointSums(result); // 챌린저별 상벌점 합계 계산
        Map<Long, MemberInfo> memberProfiles = loadMemberProfiles(result);
        Map<Long, List<ChallengerRoleType>> roleTypes = loadRoleTypes(result);
        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(result);

        List<SearchChallengerItemInfo> items = result.stream()
            .map(challenger -> toItemInfo(challenger, memberProfiles, pointSums, roleTypes, gisuGenerationMap))
            .toList();

        // 커서 페이지네이션: 다음 커서 ID값 제공
        Long nextCursor = hasNext ? result.getLast().getId() : null;

        return new SearchChallengerCursorResult(items, nextCursor, hasNext, partCounts);
    }

    // global API에서 사용하는 해당 메소드는 deprecate 예정입니다. (중복)
    @Deprecated(since = "v1.3.0", forRemoval = true)
    @Override
    public GlobalSearchChallengerCursorResult globalCursorSearch(SearchChallengerQuery query, Long cursor, int size) {
        List<Challenger> challengers = searchChallengerPort.cursorSearch(query, cursor, size);

        boolean hasNext = challengers.size() > size;
        List<Challenger> result = hasNext ? challengers.subList(0, size) : challengers;

        Map<Long, MemberInfo> memberProfiles = loadMemberProfiles(result);
        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(result);

        List<GlobalSearchChallengerItemInfo> items = result.stream()
            .map(challenger -> toGlobalItemInfo(challenger, memberProfiles, gisuGenerationMap))
            .toList();

        Long nextCursor = hasNext ? result.get(result.size() - 1).getId() : null;

        return new GlobalSearchChallengerCursorResult(items, nextCursor, hasNext);
    }

    // TODO: 아래 V2 메소드들은 왜 사용되고 있지 않은지 파악 필요 - 경운

    @Override
    public Page<ChallengerInfo> searchV2(SearchChallengerQuery query, Pageable pageable) {
        Page<Challenger> challengers = searchChallengerPort.search(query, pageable);

        return challengers.map(
            challenger -> ChallengerInfo.from(
                challenger,
                getChallengerPointUseCase.getListByChallengerId(
                    challenger.getId())
            )
        );
    }

    @Override
    public List<ChallengerInfo> searchV2(SearchChallengerQuery query, Long cursor, int size) {
        List<Challenger> challengers = searchChallengerPort.cursorSearch(query, cursor, size);

        return challengers.stream().map(challenger ->
            ChallengerInfo.from(
                challenger,
                getChallengerPointUseCase.getListByChallengerId(challenger.getId())
            )
        ).toList();
    }

    // ============================================
    // ========== Private Helper Methods ==========
    // ============================================

    /**
     * 조회된 챌린저의 파트별 인원 수를 계산
     */
    private Map<ChallengerPart, Long> buildPartCounts(SearchChallengerQuery query) {
        // 빈 Map 생성 후, 모든 ChallengerPart에 대해 0으로 초기화
        Map<ChallengerPart, Long> counts = new EnumMap<>(ChallengerPart.class);
        for (ChallengerPart part : ChallengerPart.values()) {
            counts.put(part, 0L);
        }

        // 검색 조건에 맞는 결과값을 파트에 따라 나누어서 제공함.
        counts.putAll(searchChallengerPort.countByPart(query));

        return counts;
    }

    /**
     * Overloading: Page 객체용
     * <p>
     * 챌린저의 포인트 합계를 계산 (Warning: 0.5, Out: 1.0)
     */
    private Map<Long, Double> buildPointSums(Page<Challenger> challengers) {
        return buildPointSums(challengers.getContent());
    }

    /**
     * Overloading: List 용
     * <p>
     * 챌린저의 포인트 합계를 계산 (Warning: 0.5, Out: 1.0)
     */
    private Map<Long, Double> buildPointSums(List<Challenger> challengers) {
        Set<Long> ids = challengers.stream()
            .map(Challenger::getId)
            .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return searchChallengerPort.sumPointsByChallengerIds(ids);
    }

    /**
     * 챌린저 목록에서 회원 정보 Map 제작
     */
    private Map<Long, MemberInfo> loadMemberProfiles(Page<Challenger> challengers) {
        return loadMemberProfiles(challengers.getContent());
    }

    /**
     * 챌린저 목록에 있는 모든 회원의 프로필 정보를 조회함
     */
    private Map<Long, MemberInfo> loadMemberProfiles(List<Challenger> challengers) {
        Set<Long> memberIds = challengers.stream()
            .map(Challenger::getMemberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (memberIds.isEmpty()) {
            return Map.of();
        }

        return getMemberUseCase.getProfiles(memberIds);
    }

    /**
     * 챌린저 목록에서 챌린저 역할 유형 Map 제작
     */
    private Map<Long, List<ChallengerRoleType>> loadRoleTypes(List<Challenger> challengers) {
        Set<Long> ids = challengers.stream()
            .map(Challenger::getId)
            .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        return getChallengerRoleUseCase.getRoleTypesByChallengerIds(ids);
    }

    private Map<Long, Long> loadGisuGenerationMap(List<Challenger> challengers) {
        Set<Long> gisuIds = challengers.stream()
            .map(Challenger::getGisuId)
            .collect(Collectors.toSet());

        if (gisuIds.isEmpty()) {
            return Map.of();
        }

        return getGisuUseCase.getByIds(gisuIds).stream()
            .collect(Collectors.toMap(GisuInfo::gisuId, GisuInfo::generation));
    }

    /**
     * 챌린저 검색 결과를 단일 ItemInfo로 변환
     */
    private SearchChallengerItemInfo toItemInfo(
        Challenger challenger,
        Map<Long, MemberInfo> memberProfiles,
        Map<Long, Double> pointSums,
        Map<Long, List<ChallengerRoleType>> roleTypes,
        Map<Long, Long> gisuGenerationMap
    ) {
        MemberInfo profile = memberProfiles.get(challenger.getMemberId());

        if (profile == null) {
            throw new ChallengerDomainException(ChallengerErrorCode.MEMBER_PROFILE_NOT_FOUND);
        }
        return new SearchChallengerItemInfo(
            challenger.getId(),
            challenger.getMemberId(),
            challenger.getGisuId(),
            gisuGenerationMap.getOrDefault(challenger.getGisuId(), null),
            challenger.getPart(),
            profile.name(),
            profile.nickname(),
            profile.schoolName(),
            pointSums.getOrDefault(challenger.getId(), 0.0),
            profile.profileImageLink(),
            roleTypes.getOrDefault(challenger.getId(), List.of())
        );
    }

    private GlobalSearchChallengerItemInfo toGlobalItemInfo(
        Challenger challenger,
        Map<Long, MemberInfo> memberProfiles,
        Map<Long, Long> gisuGenerationMap
    ) {
        MemberInfo profile = memberProfiles.get(challenger.getMemberId());

        if (profile == null) {
            throw new ChallengerDomainException(ChallengerErrorCode.MEMBER_PROFILE_NOT_FOUND);
        }

        return new GlobalSearchChallengerItemInfo(
            challenger.getMemberId(),
            profile.nickname(),
            profile.name(),
            profile.schoolName(),
            gisuGenerationMap.getOrDefault(challenger.getGisuId(), null),
            challenger.getPart(),
            profile.profileImageLink()
        );
    }
}
