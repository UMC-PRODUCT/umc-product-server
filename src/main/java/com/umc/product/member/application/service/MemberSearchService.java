package com.umc.product.member.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
import com.umc.product.member.application.port.in.query.SearchMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemInfo;
import com.umc.product.member.application.port.in.query.dto.SearchMemberQuery;
import com.umc.product.member.application.port.in.query.dto.SearchMemberResult;
import com.umc.product.member.application.port.out.SearchMemberPort;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
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
public class MemberSearchService implements SearchMemberUseCase {

    private final SearchMemberPort searchMemberPort;

    private final GetMemberUseCase getMemberUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetGisuUseCase getGisuUseCase;

    @Override
    public SearchMemberResult search(SearchMemberQuery query, Pageable pageable) {
        Page<Challenger> challengers = searchMemberPort.search(query, pageable);

        // 배치 데이터 로딩
        Map<Long, MemberProfileInfo> memberProfiles = loadMemberProfiles(challengers.getContent());
        Map<Long, List<ChallengerRoleType>> roleTypes = loadRoleTypes(challengers.getContent());
        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(challengers.getContent());

        Page<SearchMemberItemInfo> items = challengers.map(challenger ->
            toItemInfo(challenger, memberProfiles, roleTypes, gisuGenerationMap)
        );

        return new SearchMemberResult(items);
    }

    // ======= PRIVATE =========

    private SearchMemberItemInfo toItemInfo(
        Challenger challenger,
        Map<Long, MemberProfileInfo> memberProfiles,
        Map<Long, List<ChallengerRoleType>> roleTypes,
        Map<Long, Long> gisuGenerationMap
    ) {
        MemberProfileInfo profile = memberProfiles.get(challenger.getMemberId());

        return new SearchMemberItemInfo(
            challenger.getMemberId(),
            profile != null ? profile.name() : null,
            profile != null ? profile.nickname() : null,
            profile != null ? profile.email() : null,
            profile != null ? profile.schoolId() : null,
            profile != null ? profile.schoolName() : null,
            profile != null ? profile.profileImageLink() : null,
            challenger.getId(),
            challenger.getGisuId(),
            gisuGenerationMap.get(challenger.getGisuId()),
            challenger.getPart(),
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

    private Map<Long, List<ChallengerRoleType>> loadRoleTypes(List<Challenger> challengers) {
        Set<Long> challengerIds = challengers.stream()
            .map(Challenger::getId)
            .collect(Collectors.toSet());

        if (challengerIds.isEmpty()) {
            return Map.of();
        }

        return getChallengerRoleUseCase.getRoleTypesByChallengerIds(challengerIds);
    }

    private Map<Long, Long> loadGisuGenerationMap(List<Challenger> challengers) {
        Set<Long> gisuIds = challengers.stream()
            .map(Challenger::getGisuId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (gisuIds.isEmpty()) {
            return Map.of();
        }

        return getGisuUseCase.getByIds(gisuIds).stream()
            .collect(Collectors.toMap(GisuInfo::gisuId, GisuInfo::generation));
    }
}
