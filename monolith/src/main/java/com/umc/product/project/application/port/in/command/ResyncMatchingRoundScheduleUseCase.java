package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.out.dto.MatchingRoundTimers;

/**
 * DB에 저장된 차수 시각으로 자동 선발·결과 발표 알람을 다시 등록한다. 차수가 없으면 남은 알람을 취소한다.
 * <p>
 * DB를 새로 읽으므로 시각을 바꾼 트랜잭션이 커밋된 뒤에 부른다. 같은 차수를 여러 번 불러도 알람은 하나씩만 남는다.
 */
public interface ResyncMatchingRoundScheduleUseCase {

    MatchingRoundTimers resync(Long roundId);
}
