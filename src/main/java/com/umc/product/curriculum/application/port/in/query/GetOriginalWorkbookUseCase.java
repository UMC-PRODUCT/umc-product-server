package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

/**
 * OriginalWorkbook 조회 UseCase
 */
public interface GetOriginalWorkbookUseCase {

    /**
     * 활성 기수에서 배포된 주차 번호 목록을 조회합니다.
     *
     * @param part 파트 (null이면 모든 파트)
     * @return 배포된 주차 번호 목록 (오름차순)
     */
    List<Integer> getAvailableWeeks(ChallengerPart part);
}
