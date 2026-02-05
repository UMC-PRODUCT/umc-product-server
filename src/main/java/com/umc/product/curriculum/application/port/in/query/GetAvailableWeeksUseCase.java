package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

/**
 * 배포된 워크북 주차 목록 조회 UseCase (필터 드롭다운용)
 */
public interface GetAvailableWeeksUseCase {

    /**
     * 활성 기수에서 배포된 주차 번호 목록을 조회합니다.
     *
     * @param part 파트 (null이면 모든 파트)
     * @return 배포된 주차 번호 목록 (오름차순)
     */
    List<Integer> getAvailableWeeks(ChallengerPart part);
}
