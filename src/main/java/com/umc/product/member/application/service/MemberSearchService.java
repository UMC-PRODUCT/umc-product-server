package com.umc.product.member.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.SearchMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemInfo;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemV2Info;
import com.umc.product.member.application.port.in.query.dto.SearchMemberQuery;
import com.umc.product.member.application.port.in.query.dto.SearchMemberResult;
import com.umc.product.member.application.port.in.query.dto.SearchMemberV2Result;
import com.umc.product.member.application.port.out.SearchMemberPort;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
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
    public SearchMemberResult searchBy(SearchMemberQuery query, Pageable pageable) {
        Page<Challenger> challengers = searchMemberPort.search(query, pageable);

        // 배치 데이터 로딩
        Map<Long, MemberInfo> memberProfiles = loadMemberProfiles(challengers.getContent());
        Map<Long, List<ChallengerRoleType>> roleTypes = loadRoleTypes(challengers.getContent());
        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(challengers.getContent());

        Page<SearchMemberItemInfo> items = challengers.map(challenger ->
            toItemInfo(challenger, memberProfiles, roleTypes, gisuGenerationMap)
        );

        return new SearchMemberResult(items);
    }

    @Override
    public SearchMemberV2Result searchByV2(SearchMemberQuery query, Pageable pageable) {
        Page<Challenger> challengers = searchMemberPort.search(query, pageable);
        List<Challenger> content = challengers.getContent();

        Map<Long, MemberInfo> memberProfiles = loadMemberProfiles(content);
        Map<Long, List<ChallengerRoleType>> roleTypes = loadRoleTypes(content);
        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(content);

        // 현재 활성 기수 정보를 한 번만 조회합니다. 휴지기에는 isAdminInActiveGisu가 항상 false가 됩니다.
        Long activeGisuId = getGisuUseCase.findActiveGisu()
            .map(GisuInfo::gisuId)
            .orElse(null);

        // 검색 결과에 포함된 회원들의 활성 기수 운영진 챌린저 ID 집합을 미리 계산합니다.
        Set<Long> adminChallengerIdsInActiveGisu =
            collectAdminChallengerIdsInActiveGisu(content, activeGisuId);

        Page<SearchMemberItemV2Info> items = challengers.map(challenger -> toItemInfoV2(
            challenger, memberProfiles, roleTypes, gisuGenerationMap, adminChallengerIdsInActiveGisu
        ));

        return new SearchMemberV2Result(items);
    }

    // ======= PRIVATE =========

    private SearchMemberItemInfo toItemInfo(
        Challenger challenger,
        Map<Long, MemberInfo> memberProfiles,
        Map<Long, List<ChallengerRoleType>> roleTypes,
        Map<Long, Long> gisuGenerationMap
    ) {
        MemberInfo profile = memberProfiles.get(challenger.getMemberId());

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

    private SearchMemberItemV2Info toItemInfoV2(
        Challenger challenger,
        Map<Long, MemberInfo> memberProfiles,
        Map<Long, List<ChallengerRoleType>> roleTypes,
        Map<Long, Long> gisuGenerationMap,
        Set<Long> adminChallengerIdsInActiveGisu
    ) {
        MemberInfo profile = memberProfiles.get(challenger.getMemberId());

        return new SearchMemberItemV2Info(
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
            challenger.getStatus(),
            roleTypes.getOrDefault(challenger.getId(), List.of()),
            adminChallengerIdsInActiveGisu.contains(challenger.getId())
        );
    }

    private Map<Long, MemberInfo> loadMemberProfiles(List<Challenger> challengers) {
        Set<Long> memberIds = challengers.stream()
            .map(Challenger::getMemberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (memberIds.isEmpty()) {
            return Map.of();
        }

        return getMemberUseCase.findAllByIds(memberIds);
    }

    private Map<Long, List<ChallengerRoleType>> loadRoleTypes(List<Challenger> challengers) {
        Set<Long> challengerIds = challengers.stream()
            .map(Challenger::getId)
            .collect(Collectors.toSet());

        if (challengerIds.isEmpty()) {
            return Map.of();
        }

        return getChallengerRoleUseCase.getAllRoleTypesByChallengerIds(challengerIds);
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

    /**
     * 검색 결과로 노출된 회원들 가운데 활성 기수에 운영진 기록이 있는 챌린저 ID 집합을 구합니다.
     * <p>
     * 활성 기수에 매칭되는 검색 결과의 챌린저 ID 중 ChallengerRole이 존재하는 ID만 모읍니다.
     * 다른 기수의 챌린저 행이라도, 같은 회원이 활성 기수에 운영진을 보유하면 true가 되도록
     * 회원 단위로도 한 번 더 확장합니다.
     */
    private Set<Long> collectAdminChallengerIdsInActiveGisu(
        List<Challenger> content,
        Long activeGisuId
    ) {
        if (activeGisuId == null || content.isEmpty()) {
            return Set.of();
        }

        Set<Long> memberIdsWithAdminInActiveGisu = collectMemberIdsWithAdminInActiveGisu(content, activeGisuId);

        return content.stream()
            .filter(c -> memberIdsWithAdminInActiveGisu.contains(c.getMemberId()))
            .map(Challenger::getId)
            .collect(Collectors.toSet());
    }

    private Set<Long> collectMemberIdsWithAdminInActiveGisu(List<Challenger> content, Long activeGisuId) {
        // 활성 기수의 챌린저 행만 추려서 그 챌린저 ID로 운영진 보유 여부를 일괄 조회합니다.
        Set<Long> activeGisuChallengerIds = content.stream()
            .filter(c -> Objects.equals(c.getGisuId(), activeGisuId))
            .map(Challenger::getId)
            .collect(Collectors.toSet());

        if (activeGisuChallengerIds.isEmpty()) {
            return Set.of();
        }

        Map<Long, List<ChallengerRoleType>> activeGisuRoleTypes =
            getChallengerRoleUseCase.getAllRoleTypesByChallengerIds(activeGisuChallengerIds);

        // 운영진 보유 챌린저의 memberId를 모읍니다.
        Set<Long> adminChallengerIds = activeGisuRoleTypes.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty())
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        return content.stream()
            .filter(c -> adminChallengerIds.contains(c.getId()))
            .map(Challenger::getMemberId)
            .collect(Collectors.toSet());
    }
}
