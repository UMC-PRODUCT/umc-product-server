package com.umc.product.challenger.application.port.in.query;

public interface GetChallengerUseCase {
    // TODO: 챌린저에 대해서 public/private 정보 구분 필요 시 method 추가해서 진행하여야 함

    /**
     * 챌린저 ID로 공개 가능한 정보 조회
     */
    ChallengerPublicInfo getChallengerPublicInfo(Long challengerId);


}
