package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import java.util.List;

public interface GetChallengerUseCase {
    // TODO: 챌린저에 대해서 public/private 정보 구분 필요 시 method 추가해서 진행하여야 함

    /**
     * challengerId로 챌린저 정보 조회
     * <p>
     * 본인이 아닌, 다른 챌린저 정보를 조회하는 경우로 공개 가능한 정보만 포함합니다.
     */
    ChallengerInfo getChallengerPublicInfo(Long challengerId);

    /**
     * memberId로 해당 사용자가 가지고 있는 모든 챌린저 정보 조회
     */
    List<ChallengerInfo> getMemberChallengerList(Long memberId);
}
