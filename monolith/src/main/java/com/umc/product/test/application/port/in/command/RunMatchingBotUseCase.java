package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.BotRunResult;
import com.umc.product.test.domain.rehearsal.BotApplyParams;
import com.umc.product.test.domain.rehearsal.BotDecisionParams;

/**
 * 매칭 리허설 봇. 지원자와 PM 행동을 운영 UseCase로 대신 실행한다. 같은 차수에 다시 부르면 남은 것만 처리한다.
 */
public interface RunMatchingBotUseCase {

    /**
     * 지원 기간에만 부른다. 아니면 SEED-0305.
     */
    BotRunResult apply(Long roundId, BotApplyParams params);

    /**
     * 지원 마감 뒤부터 선발 마감 전(자동 선발 전)까지만 부른다. 아니면 SEED-0306.
     */
    BotRunResult decide(Long roundId, BotDecisionParams params);
}
