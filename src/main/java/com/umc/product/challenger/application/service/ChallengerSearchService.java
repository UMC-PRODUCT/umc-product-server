package com.umc.product.challenger.application.service;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerResult;
import com.umc.product.challenger.application.port.in.query.SearchChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.challenger.application.port.out.SearchChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.authorization.application.port.in.query.GetMemberRolesUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
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
    private final GetMemberRolesUseCase getMemberRolesUseCase;

    @Override
    public SearchChallengerResult search(SearchChallengerQuery query, Pageable pageable) {
        Page<Challenger> challengers = searchChallengerPort.search(query, pageable);
        Map<ChallengerPart, Long> partCounts = buildPartCounts(query);
        Map<Long, Double> pointSums = buildPointSums(challengers);
        Map<Long, MemberProfileInfo> memberProfiles = loadMemberProfiles(challengers);
        Map<Long, List<ChallengerRoleType>> roleTypes = loadRoleTypes(challengers.getContent());

        Page<SearchChallengerItemInfo> items = challengers.map(challenger ->
                toItemInfo(challenger, memberProfiles, pointSums, roleTypes)
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

        List<SearchChallengerItemInfo> items = result.stream()
                .map(challenger -> toItemInfo(challenger, memberProfiles, pointSums, roleTypes))
                .toList();

        Long nextCursor = hasNext ? result.get(result.size() - 1).getId() : null;

        return new SearchChallengerCursorResult(items, nextCursor, hasNext, partCounts);
    }

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
     * 챌린저의 포인트 합계를 계산 (Warning: 0.5, Out: 1.0)
     */
    private Map<Long, Double> buildPointSums(Page<Challenger> challengers) {
        return buildPointSums(challengers.getContent());
    }

    private Map<Long, Double> buildPointSums(List<Challenger> challengers) {
        Set<Long> ids = challengers.stream()
                .map(Challenger::getId)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return searchChallengerPort.sumPointsByChallengerIds(ids);
    }

    private Map<Long, MemberProfileInfo> loadMemberProfiles(Page<Challenger> challengers) {
        return loadMemberProfiles(challengers.getContent());
    }

    private Map<Long, List<ChallengerRoleType>> loadRoleTypes(List<Challenger> challengers) {
        Set<Long> ids = challengers.stream()
                .map(Challenger::getId)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return getMemberRolesUseCase.getRoleTypesByChallengerIds(ids);
    }

    private SearchChallengerItemInfo toItemInfo(
            Challenger challenger,
            Map<Long, MemberProfileInfo> memberProfiles,
            Map<Long, Double> pointSums,
            Map<Long, List<ChallengerRoleType>> roleTypes
    ) {
        MemberProfileInfo profile = memberProfiles.get(challenger.getMemberId());
        if (profile == null) {
            throw new ChallengerDomainException(ChallengerErrorCode.MEMBER_PROFILE_NOT_FOUND);
        }
        return new SearchChallengerItemInfo(
                challenger.getId(),
                challenger.getMemberId(),
                challenger.getGisuId(),
                challenger.getPart(),
                profile.name(),
                profile.nickname(),
                profile.schoolName(),
                pointSums.getOrDefault(challenger.getId(), 0.0),
                profile.profileImageLink(),
                roleTypes.getOrDefault(challenger.getId(), List.of())
        );
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
}
