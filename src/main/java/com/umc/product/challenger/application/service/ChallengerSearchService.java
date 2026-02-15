package com.umc.product.challenger.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.SearchChallengerUseCase;
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
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
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

    /**
     * 현재는 사용하고 있지 않는 것으로 보입니다.
     */
    @Override
    public SearchChallengerResult search(SearchChallengerQuery query, Pageable pageable) {
        // 페이지네이션을 적용해서 조건에 따라 챌린저 검색
        Page<Challenger> challengers = searchChallengerPort.search(query, pageable);

        // 검색된 챌린저의 파트별 인원 수 계산
        Map<ChallengerPart, Long> partCounts = buildPartCounts(query);
        // 챌린저별 상벌점 합계 계산
        Map<Long, Double> pointSums = buildPointSums(challengers);
        // 챌린저별 프로필 조회
        Map<Long, MemberProfileInfo> memberProfiles = loadMemberProfiles(challengers);
        // 챌린저별 역할 조회
        Map<Long, List<ChallengerRoleType>> roleTypes = loadRoleTypes(challengers.getContent());
        //
        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(challengers.getContent());

        Page<SearchChallengerItemInfo> items = challengers.map(challenger ->
            toItemInfo(challenger, memberProfiles, pointSums, roleTypes, gisuGenerationMap)
        );

        return new SearchChallengerResult(items, partCounts);
    }

    @Override
    public SearchChallengerCursorResult cursorSearch(SearchChallengerQuery query, Long cursor, int size) {
        List<Challenger> challengers = searchChallengerPort.cursorSearch(query, cursor, size);
        Map<ChallengerPart, Long> partCounts = buildPartCounts(query);

        boolean hasNext = challengers.size() > size;
        List<Challenger> result = hasNext ? challengers.subList(0, size) : challengers;

        Map<Long, Double> pointSums = buildPointSums(result);
        Map<Long, MemberProfileInfo> memberProfiles = loadMemberProfiles(result);
        Map<Long, List<ChallengerRoleType>> roleTypes = loadRoleTypes(result);
        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(result);

        List<SearchChallengerItemInfo> items = result.stream()
            .map(challenger -> toItemInfo(challenger, memberProfiles, pointSums, roleTypes, gisuGenerationMap))
            .toList();

        Long nextCursor = hasNext ? result.get(result.size() - 1).getId() : null;

        return new SearchChallengerCursorResult(items, nextCursor, hasNext, partCounts);
    }

    @Override
    public GlobalSearchChallengerCursorResult globalCursorSearch(SearchChallengerQuery query, Long cursor, int size) {
        List<Challenger> challengers = searchChallengerPort.cursorSearch(query, cursor, size);

        boolean hasNext = challengers.size() > size;
        List<Challenger> result = hasNext ? challengers.subList(0, size) : challengers;

        Map<Long, MemberProfileInfo> memberProfiles = loadMemberProfiles(result);
        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(result);

        List<GlobalSearchChallengerItemInfo> items = result.stream()
            .map(challenger -> toGlobalItemInfo(challenger, memberProfiles, gisuGenerationMap))
            .toList();

        Long nextCursor = hasNext ? result.get(result.size() - 1).getId() : null;

        return new GlobalSearchChallengerCursorResult(items, nextCursor, hasNext);
    }

    // ============================================
    // ========== Private Helper Methods ==========
    // ============================================

    /**
     * 조회된 챌린저의 파트별 인원 수를 계산
     */
    private Map<ChallengerPart, Long> buildPartCounts(SearchChallengerQuery query) {
        Map<ChallengerPart, Long> counts = new EnumMap<>(ChallengerPart.class);
        for (ChallengerPart part : ChallengerPart.values()) {
            counts.put(part, 0L);
        }
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
    private Map<Long, MemberProfileInfo> loadMemberProfiles(Page<Challenger> challengers) {
        return loadMemberProfiles(challengers.getContent());
    }

    private Map<Long, MemberProfileInfo> loadMemberProfiles(List<Challenger> challengers) {
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
        Map<Long, MemberProfileInfo> memberProfiles,
        Map<Long, Double> pointSums,
        Map<Long, List<ChallengerRoleType>> roleTypes,
        Map<Long, Long> gisuGenerationMap
    ) {
        MemberProfileInfo profile = memberProfiles.get(challenger.getMemberId());

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
        Map<Long, MemberProfileInfo> memberProfiles,
        Map<Long, Long> gisuGenerationMap
    ) {
        MemberProfileInfo profile = memberProfiles.get(challenger.getMemberId());

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
