package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.enums.MatchingType;

/**
 * 같은 지부·유형의 랜덤 매칭 예약을 요청한다. 실제 작업은 3차 결과 공개 트랜잭션이 커밋된 뒤 등록한다.
 */
public interface ScheduleRandomMatchingPort {

    void scheduleAfterPublication(Long chapterId, MatchingType matchingType);
}
