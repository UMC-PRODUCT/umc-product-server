package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.StartingLineResult;

/**
 * 11기 다섯 지부의 프로젝트 출발선을 만든다. 다시 부르면 PO마다 지금 상태에서 남은 단계만 이어서 실행한다.
 */
public interface SeedStartingLineUseCase {
    StartingLineResult seed();
}
