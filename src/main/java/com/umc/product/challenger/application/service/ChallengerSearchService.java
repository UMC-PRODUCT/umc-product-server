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
import com.umc.product.challenger.application.port.out.dto.ChallengerSearchBundle;
import com.umc.product.challenger.application.port.out.dto.ChallengerSearchRow;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final GetFileUseCase getFileUseCase;

    @Override
    public SearchChallengerResult offsetSearch(SearchChallengerQuery query, Pageable pageable) {
        // 검색 + 파트별 카운트를 하나의 호출로 수행 (condition 공유, member/school JOIN으로 프로필 함께 조회)
        ChallengerSearchBundle bundle = searchChallengerPort.pagingSearchWithCounts(query, pageable);
        List<ChallengerSearchRow> rows = bundle.rows();

        // 챌린저별 상벌점 합계 계산
        Map<Long, Double> pointSums = buildPointSums(rows);
        // 챌린저별 역할 조회
        Map<Long, List<ChallengerRoleType>> roleTypes = loadRoleTypes(rows);
        // 챌린저별 기수 정보 조회
        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(rows);
        // 프로필 이미지 링크 일괄 조회
        Map<String, String> profileImageLinks = loadProfileImageLinks(rows);

        // 전체 카운트 (파트별 카운트의 합)
        long totalElements = bundle.partCounts().values().stream().mapToLong(Long::longValue).sum();

        // Page Item으로 변환
        List<SearchChallengerItemInfo> items = rows.stream()
            .map(row -> toItemInfo(row, pointSums, roleTypes, gisuGenerationMap, profileImageLinks))
            .toList();

        Page<SearchChallengerItemInfo> page = new PageImpl<>(items, pageable, totalElements);

        return new SearchChallengerResult(page, bundle.partCounts());
    }

    @Override
    public SearchChallengerCursorResult cursorSearch(SearchChallengerQuery query, Long cursor, int size) {
        // 검색 + 파트별 카운트를 하나의 호출로 수행 (condition 공유, member/school JOIN으로 프로필 함께 조회)
        ChallengerSearchBundle bundle = searchChallengerPort.cursorSearchWithCounts(query, cursor, size);
        List<ChallengerSearchRow> rows = bundle.rows();

        // cursor 기반 페이지네이션 처리
        boolean hasNext = rows.size() > size;
        List<ChallengerSearchRow> result = hasNext ? rows.subList(0, size) : rows;

        Map<Long, Double> pointSums = buildPointSums(result);
        Map<Long, List<ChallengerRoleType>> roleTypes = loadRoleTypes(result);
        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(result);
        Map<String, String> profileImageLinks = loadProfileImageLinks(result);

        List<SearchChallengerItemInfo> items = result.stream()
            .map(row -> toItemInfo(row, pointSums, roleTypes, gisuGenerationMap, profileImageLinks))
            .toList();

        // 커서 페이지네이션: 다음 커서 ID값 제공
        Long nextCursor = hasNext ? result.getLast().challengerId() : null;

        return new SearchChallengerCursorResult(items, nextCursor, hasNext, bundle.partCounts());
    }

    // global API에서 사용하는 해당 메소드는 deprecate 예정입니다. (중복)
    @Deprecated(since = "v1.2.5", forRemoval = true)
    @Override
    public GlobalSearchChallengerCursorResult globalCursorSearch(SearchChallengerQuery query, Long cursor, int size) {
        ChallengerSearchBundle bundle = searchChallengerPort.cursorSearchWithCounts(query, cursor, size);
        List<ChallengerSearchRow> rows = bundle.rows();

        boolean hasNext = rows.size() > size;
        List<ChallengerSearchRow> result = hasNext ? rows.subList(0, size) : rows;

        Map<Long, Long> gisuGenerationMap = loadGisuGenerationMap(result);
        Map<String, String> profileImageLinks = loadProfileImageLinks(result);

        List<GlobalSearchChallengerItemInfo> items = result.stream()
            .map(row -> toGlobalItemInfo(row, gisuGenerationMap, profileImageLinks))
            .toList();

        Long nextCursor = hasNext ? result.get(result.size() - 1).challengerId() : null;

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
     * 챌린저의 포인트 합계를 계산
     */
    private Map<Long, Double> buildPointSums(List<ChallengerSearchRow> rows) {
        Set<Long> ids = rows.stream()
            .map(ChallengerSearchRow::challengerId)
            .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        return searchChallengerPort.sumPointsByChallengerIds(ids);
    }

    /**
     * 챌린저 목록에서 챌린저 역할 유형 Map 제작
     */
    private Map<Long, List<ChallengerRoleType>> loadRoleTypes(List<ChallengerSearchRow> rows) {
        Set<Long> ids = rows.stream()
            .map(ChallengerSearchRow::challengerId)
            .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        return getChallengerRoleUseCase.getRoleTypesByChallengerIds(ids);
    }

    private Map<Long, Long> loadGisuGenerationMap(List<ChallengerSearchRow> rows) {
        Set<Long> gisuIds = rows.stream()
            .map(ChallengerSearchRow::gisuId)
            .collect(Collectors.toSet());

        if (gisuIds.isEmpty()) {
            return Map.of();
        }

        return getGisuUseCase.getByIds(gisuIds).stream()
            .collect(Collectors.toMap(GisuInfo::gisuId, GisuInfo::generation));
    }

    /**
     * 프로필 이미지 ID 목록으로 이미지 링크를 일괄 조회
     */
    private Map<String, String> loadProfileImageLinks(List<ChallengerSearchRow> rows) {
        List<String> imageIds = rows.stream()
            .map(ChallengerSearchRow::profileImageId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (imageIds.isEmpty()) {
            return Map.of();
        }

        return getFileUseCase.getFileLinks(imageIds);
    }

    /**
     * 검색 결과 행을 단일 ItemInfo로 변환
     */
    private SearchChallengerItemInfo toItemInfo(
        ChallengerSearchRow row,
        Map<Long, Double> pointSums,
        Map<Long, List<ChallengerRoleType>> roleTypes,
        Map<Long, Long> gisuGenerationMap,
        Map<String, String> profileImageLinks
    ) {
        String profileImageLink = row.profileImageId() != null
            ? profileImageLinks.get(row.profileImageId())
            : null;

        return new SearchChallengerItemInfo(
            row.challengerId(),
            row.memberId(),
            row.gisuId(),
            gisuGenerationMap.getOrDefault(row.gisuId(), null),
            row.part(),
            row.memberName(),
            row.memberNickname(),
            row.schoolName(),
            pointSums.getOrDefault(row.challengerId(), 0.0),
            profileImageLink,
            roleTypes.getOrDefault(row.challengerId(), List.of())
        );
    }

    private GlobalSearchChallengerItemInfo toGlobalItemInfo(
        ChallengerSearchRow row,
        Map<Long, Long> gisuGenerationMap,
        Map<String, String> profileImageLinks
    ) {
        String profileImageLink = row.profileImageId() != null
            ? profileImageLinks.get(row.profileImageId())
            : null;

        return new GlobalSearchChallengerItemInfo(
            row.memberId(),
            row.memberNickname(),
            row.memberName(),
            row.schoolName(),
            gisuGenerationMap.getOrDefault(row.gisuId(), null),
            row.part(),
            profileImageLink
        );
    }
}
