package com.umc.product.challenger.application.service;

import com.umc.product.challenger.application.port.in.query.SearchChallengerQuery;
import com.umc.product.challenger.application.port.in.query.SearchChallengerResult;
import com.umc.product.challenger.application.port.in.query.SearchChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.challenger.application.port.out.SearchChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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

    @Override
    public SearchChallengerResult search(SearchChallengerQuery query, Pageable pageable) {
        Page<Challenger> challengers = searchChallengerPort.search(query, pageable);
        Map<ChallengerPart, Long> partCounts = buildPartCounts(query);
        Map<Long, Double> pointSums = buildPointSums(challengers);
        Map<Long, MemberProfileInfo> memberProfiles = loadMemberProfiles(challengers);

        Page<SearchChallengerItemInfo> items = challengers.map(challenger -> {
            MemberProfileInfo profile = memberProfiles.get(challenger.getMemberId());
            return new SearchChallengerItemInfo(
                    challenger.getId(),
                    challenger.getMemberId(),
                    challenger.getGisuId(),
                    challenger.getPart(),
                    profile != null ? profile.name() : null,
                    profile != null ? profile.nickname() : null,
                    pointSums.getOrDefault(challenger.getId(), 0.0),
                    profile != null ? profile.profileImageLink() : null
            );
        });

        return new SearchChallengerResult(items, partCounts);
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
        Set<Long> ids = challengers.getContent().stream()
                .map(Challenger::getId)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return searchChallengerPort.sumPointsByChallengerIds(ids);
    }


    private Map<Long, MemberProfileInfo> loadMemberProfiles(Page<Challenger> challengers) {
        return challengers.getContent().stream()
                .map(Challenger::getMemberId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        getMemberUseCase::getProfile
                ));
    }
}
