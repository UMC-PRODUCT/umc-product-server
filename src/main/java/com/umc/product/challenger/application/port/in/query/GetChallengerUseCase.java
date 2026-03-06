package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.common.domain.enums.ChallengerPart;
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

    /**
     * 여러 기수 ID로 챌린저 정보 일괄 조회 (IN 쿼리 1회)
     *
     * @param gisuIds 기수 ID 목록
     * @return 해당 기수들의 챌린저 정보 목록
     */
    List<ChallengerInfo> getByGisuIds(List<Long> gisuIds);

    /**
     * memberId로 해당 사용자가 가지고 있는 가장 최근 챌린저 정보 조회
     */
    ChallengerInfoWithStatus getLatestActiveChallengerByMemberId(Long memberId);

    /**
     * 각 멤버별 가장 최근 기수(gisuId 최대값)의 챌린저 정보 조회
     * <p>
     * 한 멤버가 여러 기수에 걸쳐 챌린저인 경우, 가장 최근 기수의 챌린저 1건만 반환합니다.
     */
    List<ChallengerInfo> getLatestPerMember();

    /**
     * 특정 멤버가 특정 기수에서 보유한 모든 파트를 조회
     * <p>
     * 한 멤버가 같은 기수에 복수의 챌린저(파트)를 가질 수 있습니다. (ex. iOS파트장 + Spring챌린저)
     *
     * @return 해당 기수에서 멤버가 보유한 파트 Set. 챌린저가 없으면 빈 Set 반환.
     */
    Set<ChallengerPart> getPartsByMemberAndGisu(Long memberId, Long gisuId);
}
