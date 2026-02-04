package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GetChallengerUseCase {
    // TODO: 챌린저에 대해서 public/private 정보 구분 필요 시 method 추가해서 진행하여야 함

    /**
     * challengerId로 챌린저 정보 조회
     * <p>
     * 본인이 아닌, 다른 챌린저 정보를 조회하는 경우로 공개 가능한 정보만 포함합니다.
     */
    ChallengerInfo getChallengerPublicInfo(Long challengerId);

    /**
     * memberId와 gisuId로 챌린저 정보 조회
     */
    ChallengerInfo getByMemberIdAndGisuId(Long memberId, Long gisuId);

    /**
     * memberId와 gisuId로 ACTIVE 챌린저 정보 조회
     */
    ChallengerInfo getActiveByMemberIdAndGisuId(Long memberId, Long gisuId);

    /**
     * memberId로 해당 사용자가 가지고 있는 모든 챌린저 정보 조회
     */
    List<ChallengerInfo> getMemberChallengerList(Long memberId);

    /**
     * memberId로 해당 사용자가 가지고 있는 가장 최근 챌린저 정보 조회
     */
    ChallengerInfo getLatestActiveChallengerByMemberId(Long memberId);

    /**
     * 여러 challengerId로 챌린저 정보 배치 조회
     *
     * @param challengerIds 챌린저 ID 목록
     * @return challengerId → ChallengerInfo Map
     */
    Map<Long, ChallengerInfo> getChallengerPublicInfoByIds(Set<Long> challengerIds);

    /**
     * 기수 ID로 해당 기수의 모든 챌린저 정보 조회
     *
     * @param gisuId 기수 ID
     * @return 해당 기수의 챌린저 정보 목록
     */
    List<ChallengerInfo> getByGisuId(Long gisuId);
}
