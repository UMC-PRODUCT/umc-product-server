package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GetChallengerUseCase {
    // TODO: 챌린저에 대해서 public/private 정보 구분 필요 시 method 추가해서 진행하여야 함

    /**
     * challengerId로 챌린저 정보를 조회합니다.
     */
    ChallengerInfo getById(Long challengerId);

    Optional<ChallengerInfo> findById(Long challengerId);

    /**
     * challengerId로 챌린저를 검색합니다. 존재하지 않을 경우 null을 반환합니다.
     *
     * @deprecated {@link #findById}를 사용하도록 변경해주세요.
     */
    @Deprecated(since = "v1.5.0", forRemoval = true)
    ChallengerInfo findByIdOrNull(Long challengerId);

    /**
     * memberId와 gisuId로 챌린저 정보 조회
     */
    ChallengerInfo getByMemberIdAndGisuId(Long memberId, Long gisuId);

    /**
     * memberId와 gisuId로 챌린저 정보 조회 (없으면 Optional.empty())
     */
    Optional<ChallengerInfo> findByMemberIdAndGisuId(Long memberId, Long gisuId);

    /**
     * memberId와 gisuId로 ACTIVE 챌린저 정보 조회
     */
    ChallengerInfo getActiveByMemberIdAndGisuId(Long memberId, Long gisuId);

    /**
     * memberId로 해당 사용자가 가지고 있는 모든 챌린저 정보 조회
     */
    List<ChallengerInfo> getAllByMemberId(Long memberId);

    /**
     * 여러 challengerId로 챌린저 정보 배치 조회
     *
     * @param challengerIds 챌린저 ID 목록
     * @return challengerId → ChallengerInfo Map
     * @deprecated Map을 사용하는 것이 아니라 List를 반환하도록 변경해야 합니다. 현재 사용중인 곳에서 모두 제거하게 되면 변경 예정입니다.
     */
    @Deprecated(since = "v1.5.0", forRemoval = true)
    Map<Long, ChallengerInfo> getAllByIdsAsMap(Set<Long> challengerIds);

    List<ChallengerInfo> getAllByIds(Set<Long> challengerIds);

    /**
     * 기수 ID로 해당 기수의 모든 챌린저 정보 조회
     *
     * @param gisuId 기수 ID
     * @return 해당 기수의 챌린저 정보 목록
     */
    List<ChallengerInfo> getAllByGisuId(Long gisuId);

    /**
     * memberId로 해당 사용자가 가지고 있는 가장 최근 챌린저 정보 조회
     */
    ChallengerInfoWithStatus getLatestActiveChallengerByMemberId(Long memberId);

    /**
     * 기수 ID로 해당 기수의 모든 챌린저 ID 조회 (상벌점 제외)
     * <p>
     * 대상자 집계 등 상벌점이 불필요한 경우 사용합니다.
     *
     * @deprecated {@link ChallengerInfo}가 아닌 별도의 Info단 DTO를 생성해서 사용하도록 합니다. 그 전까지는 {@link #getAllByGisuId}를 사용해주세요.
     */
    @Deprecated(since = "v1.5.0", forRemoval = true)
    List<ChallengerInfo> getAllByGisuIdWithoutChallengerPoints(Long gisuId);

    /**
     * 각 멤버별 가장 최근 기수의 챌린저 조회 (상벌점 제외)
     * <p>
     * 대상자 집계 등 상벌점이 불필요한 경우 사용합니다.
     */
    List<ChallengerInfo> getAllLatestGisuPerMemberWithoutChallengerPoints();

}
