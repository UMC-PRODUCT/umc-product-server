package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ActivityPeriodSummary;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import java.util.List;
import java.util.Map;

/**
 * 회원의 기수별 활동일 합산을 조회하는 UseCase 입니다.
 * <p>
 * 활동일은 챌린저 상태가 ACTIVE 또는 GRADUATED인 기수에 한해 산정되며,
 * 진행 중인 기수는 요청 시점(now)까지의 일수로 계산합니다.
 * EXPELLED/WITHDRAWN 챌린저는 합산에서 제외됩니다.
 */
public interface GetChallengerActivityPeriodUseCase {

    /**
     * memberId만 가지고 단독으로 활동일 요약을 계산합니다. 내부에서 챌린저와 기수 정보를 조회합니다.
     */
    ActivityPeriodSummary getActivityPeriodByMemberId(Long memberId);

    /**
     * 이미 조회된 챌린저/기수 데이터를 받아 활동일 요약을 계산합니다.
     * <p>
     * BFF처럼 동일 트랜잭션 내에서 이미 일괄 조회한 데이터를 재사용해 중복 쿼리를 피하고자 할 때 사용합니다.
     *
     * @param challengers      회원의 챌린저 정보 목록
     * @param gisuInfosByGisuId 챌린저들이 참여한 기수 정보 (gisuId 기준)
     */
    ActivityPeriodSummary calculateActivityPeriod(
        List<ChallengerInfo> challengers,
        Map<Long, GisuInfo> gisuInfosByGisuId
    );
}
